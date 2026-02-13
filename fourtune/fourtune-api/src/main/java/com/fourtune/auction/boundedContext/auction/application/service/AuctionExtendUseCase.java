package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionPolicy;
import com.fourtune.auction.boundedContext.auction.domain.constant.BidPolicy;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.boundedContext.user.application.service.UserFacade;
import com.fourtune.common.global.error.ErrorCode;
import com.fourtune.common.global.error.exception.BusinessException;
import com.fourtune.common.global.eventPublisher.EventPublisher;
import com.fourtune.common.shared.auction.event.AuctionExtendedEvent;
import com.fourtune.common.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.common.global.config.EventPublishingConfig;
import com.fourtune.common.global.outbox.service.OutboxService;
import com.fourtune.common.shared.auction.kafka.AuctionEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 경매 자동 연장 UseCase
 * - 종료 5분 전 입찰 시 자동 연장
 * - 연장 횟수 제한
 */
@Service
@RequiredArgsConstructor
public class AuctionExtendUseCase {

    private static final String AGGREGATE_TYPE_AUCTION = "Auction";

    private final AuctionSupport auctionSupport;
    private final EventPublisher eventPublisher;
    private final BidPolicy bidPolicy;
    private final UserFacade userFacade;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

    /**
     * [진입점] 경매 자동 연장
     * 동시성 제어: Pessimistic Lock 적용
     */
    @Transactional
    public void extendAuction(Long auctionId) {
        // 1. 경매 조회 (Pessimistic Lock 적용)
        AuctionItem auctionItem = auctionSupport.findByIdWithLockOrThrow(auctionId);
        
        // 2. 내부 메서드로 위임
        extendAuctionInternal(auctionItem);
    }

    /**
     * [진입점] 경매 자동 연장 (엔티티 직접 전달)
     * 이미 Lock이 획득된 엔티티를 사용하여 중복 Lock 방지
     * 외부 UseCase에서 이미 Lock을 획득한 경우 사용
     */
    @Transactional
    public void extendAuction(AuctionItem auctionItem) {
        extendAuctionInternal(auctionItem);
    }

    /**
     * [내부 로직] 경매 자동 연장 (엔티티 직접 전달)
     * - private 선언: 외부 호출 방지
     * - @Transactional 제거: 부모 트랜잭션을 그대로 따라감
     */
    private void extendAuctionInternal(AuctionItem auctionItem) {
        // 1. 연장 가능 여부 확인
        validateExtendable(auctionItem);
        
        // 2. 경매 시간 연장 (3분 연장)
        auctionItem.extend(AuctionPolicy.AUTO_EXTEND_MINUTES);
        
        // 3. DB 저장 (dirty checking)
        
        // 4. 이벤트 발행
        Long aggregateId = auctionItem.getId();
        AuctionExtendedEvent extendedEvent = new AuctionExtendedEvent(
                auctionItem.getId(),
                auctionItem.getAuctionEndTime()  // 새로운 종료 시간
        );
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, aggregateId, AuctionEventType.AUCTION_EXTENDED.name(), Map.of("eventType", AuctionEventType.AUCTION_EXTENDED.name(), "aggregateId", aggregateId, "data", extendedEvent));
        } else {
            eventPublisher.publish(extendedEvent);
        }

        // 5. Search 인덱싱 전용 이벤트 발행 (스냅샷 형태)
        String thumbnailUrl = extractThumbnailUrl(auctionItem);
        String sellerName = userFacade.getNicknamesByIds(Set.of(auctionItem.getSellerId())).getOrDefault(auctionItem.getSellerId(), null);
        AuctionItemUpdatedEvent itemUpdatedEvent = new AuctionItemUpdatedEvent(
                auctionItem.getId(),
                auctionItem.getSellerId(),
                sellerName,
                auctionItem.getTitle(),
                auctionItem.getDescription(),
                auctionItem.getCategory().toString(),
                auctionItem.getStatus().toString(),
                auctionItem.getStartPrice(),
                auctionItem.getCurrentPrice(),
                auctionItem.getBuyNowPrice(),
                auctionItem.getBuyNowEnabled(),
                auctionItem.getAuctionStartTime(),
                auctionItem.getAuctionEndTime(),
                thumbnailUrl,
                auctionItem.getCreatedAt(),
                auctionItem.getUpdatedAt(),
                auctionItem.getViewCount(),
                auctionItem.getBidCount(),
                auctionItem.getWatchlistCount()
        );
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, aggregateId, AuctionEventType.AUCTION_ITEM_UPDATED.name(), Map.of("eventType", AuctionEventType.AUCTION_ITEM_UPDATED.name(), "aggregateId", aggregateId, "data", itemUpdatedEvent));
        } else {
            eventPublisher.publish(itemUpdatedEvent);
        }
    }

    /**
     * 썸네일 URL 추출
     */
    private String extractThumbnailUrl(AuctionItem auctionItem) {
        if (auctionItem.getImages() == null || auctionItem.getImages().isEmpty()) {
            return null;
        }
        return auctionItem.getImages().stream()
                .filter(ItemImage::getIsThumbnail)
                .findFirst()
                .map(ItemImage::getImageUrl)
                .orElseGet(() -> auctionItem.getImages().get(0).getImageUrl()); // 썸네일이 없으면 첫 번째 이미지
    }

    /**
     * 자동 연장 필요 여부 확인
     */
    public boolean needsExtension(Long auctionId, LocalDateTime bidTime) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 종료 시간과 입찰 시간 차이 계산
        java.time.Duration remaining = java.time.Duration.between(
                bidTime, 
                auctionItem.getAuctionEndTime()
        );
        
        // 3. AuctionPolicy.AUTO_EXTEND_THRESHOLD_MINUTES (5분) 이내면 true
        return remaining.toMinutes() <= AuctionPolicy.AUTO_EXTEND_THRESHOLD_MINUTES;
    }

    /**
     * 연장 가능 여부 검증
     */
    private void validateExtendable(AuctionItem auctionItem) {
        // ACTIVE 상태인지 확인
        if (auctionItem.getStatus() != com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        
        // 연장 횟수 제한 확인
        if (auctionItem.getExtensionCount() >= bidPolicy.getMaxAutoExtendCount()) {
            throw new BusinessException(ErrorCode.AUCTION_MAX_EXTENSION_REACHED);
        }
    }

}
