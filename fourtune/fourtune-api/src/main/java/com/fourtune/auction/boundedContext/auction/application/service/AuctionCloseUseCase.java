package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.boundedContext.user.application.service.UserFacade;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.auction.event.AuctionClosedEvent;
import com.fourtune.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.kafka.auction.AuctionEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

/**
 * 경매 종료 UseCase
 * - 경매 시간 종료 시 처리
 * - 낙찰자 결정
 * - Order 생성
 * - 실패한 입찰들 처리
 */
@Service
@RequiredArgsConstructor
public class AuctionCloseUseCase {

    private static final String AGGREGATE_TYPE_AUCTION = "Auction";

    private final AuctionSupport auctionSupport;
    private final BidSupport bidSupport;
    private final OrderCreateUseCase orderCreateUseCase;
    private final EventPublisher eventPublisher;
    private final UserFacade userFacade;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

    /**
     * 경매 종료 처리
     * 동시성 제어: Pessimistic Lock 적용
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void closeAuction(Long auctionId) {
        // 1. 경매 조회 (Pessimistic Lock 적용)
        AuctionItem auctionItem = auctionSupport.findByIdWithLockOrThrow(auctionId);
        
        // 2. 종료 가능 여부 확인
        validateCloseable(auctionItem);
        
        // 3. 최고가 입찰 조회
        java.util.Optional<Bid> highestBidOpt = bidSupport.findHighestBid(auctionId);
        
        if (highestBidOpt.isPresent()) {
            // 4-1. 낙찰자가 있는 경우
            Bid winningBid = highestBidOpt.get();
            
            // 경매 상태 변경 (ACTIVE -> ENDED -> SOLD)
            auctionItem.close();  // ACTIVE -> ENDED
            auctionItem.sell();   // ENDED -> SOLD
            
            // 낙찰 입찰 처리
            winningBid.win();
            bidSupport.save(winningBid);
            
            // Order 생성 (엔티티 직접 전달하여 중복 Lock 방지)
            String orderId = orderCreateUseCase.createWinningOrder(
                    auctionItem,
                    winningBid.getBidderId(),
                    winningBid.getBidAmount()
            );
            
            // 실패한 입찰들 처리
            bidSupport.failAllActiveBids(auctionId);
            
            // 이벤트 발행
            AuctionClosedEvent closedEvent = new AuctionClosedEvent(
                    auctionId,
                    auctionItem.getTitle(),
                    auctionItem.getSellerId(),
                    winningBid.getBidderId(),
                    winningBid.getBidAmount(),
                    orderId
            );
            if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
                outboxService.append(AGGREGATE_TYPE_AUCTION, auctionId, AuctionEventType.AUCTION_CLOSED.name(), Map.of("eventType", AuctionEventType.AUCTION_CLOSED.name(), "aggregateId", auctionId, "data", closedEvent));
            } else {
                eventPublisher.publish(closedEvent);
            }

            // Search 인덱싱 전용 이벤트 발행 (스냅샷 형태)
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
        } else {
            // 4-2. 낙찰자가 없는 경우 (입찰이 없었음)
            auctionItem.close();

            // 이벤트 발행
            AuctionClosedEvent closedEventNoWinner = new AuctionClosedEvent(
                    auctionId,
                    auctionItem.getTitle(),
                    auctionItem.getSellerId(),
                    null,  // 낙찰자 없음
                    null,  // 낙찰가 없음
                    null   // 주문번호 없음
            );
            if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
                outboxService.append(AGGREGATE_TYPE_AUCTION, auctionId, AuctionEventType.AUCTION_CLOSED.name(), Map.of("eventType", AuctionEventType.AUCTION_CLOSED.name(), "aggregateId", auctionId, "data", closedEventNoWinner));
            } else {
                eventPublisher.publish(closedEventNoWinner);
            }

            // Search 인덱싱 전용 이벤트 발행 (스냅샷 형태)
            String thumbnailUrl = extractThumbnailUrl(auctionItem);
            String sellerName = userFacade.getNicknamesByIds(java.util.Set.of(auctionItem.getSellerId())).getOrDefault(auctionItem.getSellerId(), null);
            AuctionItemUpdatedEvent itemUpdatedEventNoWinner = new AuctionItemUpdatedEvent(
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
                outboxService.append(AGGREGATE_TYPE_AUCTION, auctionId, AuctionEventType.AUCTION_ITEM_UPDATED.name(), Map.of("eventType", AuctionEventType.AUCTION_ITEM_UPDATED.name(), "aggregateId", auctionId, "data", itemUpdatedEventNoWinner));
            } else {
                eventPublisher.publish(itemUpdatedEventNoWinner);
            }
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
     * 종료 가능 여부 검증
     */
    private void validateCloseable(AuctionItem auctionItem) {
        // ACTIVE 상태인지 확인
        if (auctionItem.getStatus() != com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus.ACTIVE) {
            throw new com.fourtune.common.global.error.exception.BusinessException(
                    com.fourtune.common.global.error.ErrorCode.AUCTION_NOT_ACTIVE
            );
        }
        
        // 종료 시간이 지났는지 확인
        if (java.time.LocalDateTime.now().isBefore(auctionItem.getAuctionEndTime())) {
            throw new com.fourtune.common.global.error.exception.BusinessException(
                    com.fourtune.common.global.error.ErrorCode.AUCTION_NOT_MODIFIABLE
            );
        }
    }

}
