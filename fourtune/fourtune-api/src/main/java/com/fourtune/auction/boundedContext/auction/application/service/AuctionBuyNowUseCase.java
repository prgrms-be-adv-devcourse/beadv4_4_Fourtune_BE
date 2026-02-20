package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionPolicy;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.boundedContext.user.application.service.UserFacade;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.auction.event.AuctionBuyNowEvent;
import com.fourtune.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.shared.kafka.auction.AuctionEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * 즉시구매 UseCase
 * - 단일 경매 즉시구매 처리 (경매 상세 페이지에서 "즉시구매" 버튼)
 * - 경매 즉시 종료
 * - Order 생성
 * - 장바구니의 해당 경매 아이템 만료 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuctionBuyNowUseCase {

    private static final String AGGREGATE_TYPE_AUCTION = "Auction";

    private final AuctionSupport auctionSupport;
    private final OrderSupport orderSupport;
    private final OrderCreateUseCase orderCreateUseCase;
    private final CartSupport cartSupport;
    private final EventPublisher eventPublisher;
    private final UserFacade userFacade;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

    /**
     * 즉시구매 처리
     * 동시성 제어: Pessimistic Lock 적용
     */
    @Transactional
    public String executeBuyNow(Long auctionId, Long buyerId) {
        // 1. 경매 조회 (Pessimistic Lock 적용)
        AuctionItem auctionItem = auctionSupport.findByIdWithLockOrThrow(auctionId);
        
        // 1.5 시작 시각이 지났는데 아직 SCHEDULED인 경우 ACTIVE로 전환 (스케줄러 대기 없이 즉시구매 가능)
        if (auctionItem.getStatus() == AuctionStatus.SCHEDULED
                && auctionItem.getAuctionStartTime() != null
                && !LocalDateTime.now().isBefore(auctionItem.getAuctionStartTime())) {
            auctionItem.start();
            auctionSupport.save(auctionItem);
            log.debug("즉시구매: 경매 시작 시각 경과로 SCHEDULED → ACTIVE 전환, auctionId={}", auctionId);
        }
        
        // 2. 즉시구매 가능 여부 검증
        validateBuyNowAvailable(auctionItem, buyerId);
        
        // 3. 경매 상태 변경 (ACTIVE -> SOLD_BY_BUY_NOW)
        auctionItem.executeBuyNow();
        auctionSupport.save(auctionItem);
        
        // 4. 주문 생성 (엔티티 직접 전달하여 중복 Lock 방지)
        String orderId = orderCreateUseCase.createBuyNowOrder(auctionItem, buyerId, auctionItem.getBuyNowPrice());
        
        // 5. 장바구니의 해당 경매 아이템 만료 처리
        try {
            cartSupport.expireCartItemsByAuctionId(auctionId);
        } catch (Exception e) {
            log.warn("장바구니 아이템 만료 처리 실패: auctionId={}, error={}", auctionId, e.getMessage());
        }
        
        // 6. 이벤트 발행 (AuctionBuyNowEvent)
        AuctionBuyNowEvent buyNowEvent = new AuctionBuyNowEvent(
                auctionId,
                auctionItem.getSellerId(),
                buyerId,
                auctionItem.getBuyNowPrice(),
                orderId,
                LocalDateTime.now()
        );
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, auctionId, AuctionEventType.AUCTION_BUY_NOW.name(), Map.of("eventType", AuctionEventType.AUCTION_BUY_NOW.name(), "aggregateId", auctionId, "data", buyNowEvent));
        } else {
            eventPublisher.publish(buyNowEvent);
        }

        // 7. Search 인덱싱 전용 이벤트 발행 (스냅샷 형태)
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
            outboxService.append(AGGREGATE_TYPE_AUCTION, auctionId, AuctionEventType.AUCTION_ITEM_UPDATED.name(), Map.of("eventType", AuctionEventType.AUCTION_ITEM_UPDATED.name(), "aggregateId", auctionId, "data", itemUpdatedEvent));
        } else {
            eventPublisher.publish(itemUpdatedEvent);
        }

        log.info("즉시구매 처리 완료: auctionId={}, buyerId={}, orderId={}", auctionId, buyerId, orderId);
        
        // 8. orderId 반환 (결제 프로세스로 전달)
        return orderId;
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
     * 즉시구매 가능 여부 검증
     */
    private void validateBuyNowAvailable(AuctionItem auctionItem, Long buyerId) {
        // 1. buyNowEnabled = true 인지
        if (!auctionItem.getBuyNowEnabled()) {
            throw new BusinessException(ErrorCode.BUY_NOW_NOT_ENABLED);
        }

        // 2. 정책에 의해 즉시구매 비활성화되었는지 (경매당 3회 Circuit Breaker)
        if (Boolean.TRUE.equals(auctionItem.getBuyNowDisabledByPolicy())) {
            throw new BusinessException(ErrorCode.BUY_NOW_DISABLED_BY_POLICY);
        }

        // 3. 경매당 유저당 2회 제한 (이미 2회 미결제 시 3번째 시도 거부)
        long cancelledBuyNowCount = orderSupport.countCancelledBuyNowOrdersByAuctionAndWinner(
                auctionItem.getId(), buyerId);
        if (cancelledBuyNowCount >= AuctionPolicy.BUY_NOW_RECOVERY_MAX_PER_USER) {
            throw new BusinessException(ErrorCode.BUY_NOW_USER_LIMIT_REACHED);
        }
        
        // 4. buyNowPrice != null 인지
        if (auctionItem.getBuyNowPrice() == null) {
            throw new BusinessException(ErrorCode.BUY_NOW_PRICE_NOT_SET);
        }
        
        // 5. status = ACTIVE 인지 (실제 상태별 구체 메시지 + 원인 로그)
        if (auctionItem.getStatus() != AuctionStatus.ACTIVE) {
            log.warn("즉시구매 불가: 경매 상태가 ACTIVE가 아님. auctionId={}, actualStatus={}", auctionItem.getId(), auctionItem.getStatus());
            if (auctionItem.getStatus() == AuctionStatus.SOLD_BY_BUY_NOW) {
                throw new BusinessException(ErrorCode.AUCTION_ALREADY_SOLD_BY_BUY_NOW);
            }
            if (auctionItem.getStatus() == AuctionStatus.ENDED || auctionItem.getStatus() == AuctionStatus.SOLD) {
                throw new BusinessException(ErrorCode.AUCTION_ALREADY_ENDED);
            }
            throw new BusinessException(ErrorCode.AUCTION_NOT_ACTIVE);
        }
        
        // 6. 본인이 판매자가 아닌지
        if (auctionItem.getSellerId().equals(buyerId)) {
            throw new BusinessException(ErrorCode.CANNOT_BUY_OWN_ITEM);
        }
    }

}
