package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.boundedContext.user.application.service.UserFacade;
import com.fourtune.common.global.eventPublisher.EventPublisher;
import com.fourtune.common.shared.auction.dto.AuctionItemUpdateRequest;

import java.util.Set;
import com.fourtune.common.shared.auction.event.AuctionUpdatedEvent;
import com.fourtune.common.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.common.global.config.EventPublishingConfig;
import com.fourtune.common.global.outbox.service.OutboxService;
import com.fourtune.common.shared.auction.kafka.AuctionEventType;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 경매 수정 UseCase
 * - 경매 정보 수정
 * - 진행중인 경매는 일부만 수정 가능
 */
@Service
@RequiredArgsConstructor
public class AuctionUpdateUseCase {

    private static final String AGGREGATE_TYPE_AUCTION = "Auction";

    private final AuctionSupport auctionSupport;
    private final BidSupport bidSupport;
    private final EventPublisher eventPublisher;
    private final UserFacade userFacade;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

    /**
     * 경매 정보 수정
     */
    @Transactional
    public void updateAuction(Long auctionId, Long userId, AuctionItemUpdateRequest request) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 판매자 권한 확인
        auctionSupport.validateSeller(auctionItem, userId);
        
        // 3. 수정 가능 여부 확인
        validateUpdateable(auctionItem);
        
        // 4. 엔티티 수정 (엔티티의 update 메서드 호출)
        auctionItem.update(
                request.title(),
                request.description(),
                request.buyNowPrice(),
                request.buyNowPrice() != null  // buyNowEnabled
        );
        
        // 5. DB 저장 (dirty checking으로 자동 저장)
        
        // 6. 이벤트 발행
        Long aggregateId = auctionItem.getId();
        String sellerName = userFacade.getNicknamesByIds(Set.of(auctionItem.getSellerId())).getOrDefault(auctionItem.getSellerId(), null);
        AuctionUpdatedEvent updatedEvent = new AuctionUpdatedEvent(
                auctionItem.getId(),
                auctionItem.getSellerId(),
                sellerName,
                auctionItem.getTitle(),
                auctionItem.getDescription(),
                auctionItem.getBuyNowPrice(),
                auctionItem.getBuyNowEnabled(),
                auctionItem.getCategory().toString()
        );
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, aggregateId, AuctionEventType.AUCTION_UPDATED.name(), Map.of("eventType", AuctionEventType.AUCTION_UPDATED.name(), "aggregateId", aggregateId, "data", updatedEvent));
        } else {
            eventPublisher.publish(updatedEvent);
        }

        // 7. Search 인덱싱 전용 이벤트 발행 (스냅샷 형태)
        String thumbnailUrl = extractThumbnailUrl(auctionItem);
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
     * 수정 가능 여부 검증
     */
    private void validateUpdateable(AuctionItem auctionItem) {
        com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus status = 
                auctionItem.getStatus();
        
        // ENDED, SOLD, SOLD_BY_BUY_NOW, CANCELLED 상태는 수정 불가
        if (status == com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.ENDED ||
            status == com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.SOLD ||
            status == com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.SOLD_BY_BUY_NOW ||
            status == com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.CANCELLED) {
            throw new com.fourtune.common.global.error.exception.BusinessException(
                    com.fourtune.common.global.error.ErrorCode.AUCTION_NOT_MODIFIABLE
            );
        }
        
        // ACTIVE 상태에서 입찰이 있으면 제한적 수정만 가능
        // (현재는 엔티티의 update 메서드에서 제목, 설명, 즉시구매가만 수정 가능)
    }

}
