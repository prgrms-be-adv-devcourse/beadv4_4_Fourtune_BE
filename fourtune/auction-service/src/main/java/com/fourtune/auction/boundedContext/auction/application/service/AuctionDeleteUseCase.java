package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import com.fourtune.auction.port.out.UserPort;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.auction.event.AuctionDeletedEvent;
import com.fourtune.shared.auction.event.AuctionItemDeletedEvent;
import com.fourtune.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.shared.kafka.auction.AuctionEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

/**
 * 경매 삭제 UseCase
 * - 경매 삭제 (soft delete or hard delete)
 * - 입찰이 있으면 삭제 불가
 */
@Service
@RequiredArgsConstructor
public class AuctionDeleteUseCase {

    private static final String AGGREGATE_TYPE_AUCTION = "Auction";

    private final AuctionSupport auctionSupport;
    private final AuctionItemRepository auctionItemRepository;
    private final EventPublisher eventPublisher;
    private final UserPort userPort;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

    /**
     * 경매 삭제
     */
    @Transactional
    public void deleteAuction(Long auctionId, Long userId) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 판매자 권한 확인
        auctionSupport.validateSeller(auctionItem, userId);
        
        // 3. 삭제 가능 여부 확인
        auctionSupport.validateDeletable(auctionItem);
        
        // 4. 삭제 전 정보 저장 (이벤트 발행용)
        Long deletedAuctionId = auctionItem.getId();
        Long sellerId = auctionItem.getSellerId();
        String title = auctionItem.getTitle();
        com.fourtune.auction.boundedContext.auction.domain.constant.Category category = 
                auctionItem.getCategory();
        
        // 5. 삭제 처리 (hard delete)
        auctionItemRepository.delete(auctionItem);
        
        // 6. 이벤트 발행
        AuctionDeletedEvent deletedEvent = new AuctionDeletedEvent(deletedAuctionId, sellerId, title, category.toString());
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, deletedAuctionId, AuctionEventType.AUCTION_DELETED.name(), Map.of("eventType", AuctionEventType.AUCTION_DELETED.name(), "aggregateId", deletedAuctionId, "data", deletedEvent));
        } else {
            eventPublisher.publish(deletedEvent);
        }

        // 7. Search 인덱싱 전용 이벤트 발행 (auctionId만 필요)
        AuctionItemDeletedEvent itemDeletedEvent = new AuctionItemDeletedEvent(deletedAuctionId);
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, deletedAuctionId, AuctionEventType.AUCTION_ITEM_DELETED.name(), Map.of("eventType", AuctionEventType.AUCTION_ITEM_DELETED.name(), "aggregateId", deletedAuctionId, "data", itemDeletedEvent));
        } else {
            eventPublisher.publish(itemDeletedEvent);
        }
    }

    /**
     * 경매 취소 (soft delete)
     */
    @Transactional
    public void cancelAuction(Long auctionId, Long userId) {
        // 1. 경매 조회
        AuctionItem auctionItem = auctionSupport.findByIdOrThrow(auctionId);
        
        // 2. 판매자 권한 확인
        auctionSupport.validateSeller(auctionItem, userId);
        
        // 3. 취소 가능 여부 확인 (입찰이 있으면 취소 불가)
        if (auctionItem.getBidCount() > 0) {
            throw new com.fourtune.core.error.exception.BusinessException(
                    com.fourtune.core.error.ErrorCode.AUCTION_HAS_BIDS
            );
        }
        
        // 4. 상태 변경 (CANCELLED)
        auctionItem.cancel();
        
        // 5. Search 인덱싱 전용 이벤트 발행 (스냅샷 형태)
        Long aggregateId = auctionItem.getId();
        String thumbnailUrl = extractThumbnailUrl(auctionItem);
        String sellerName = userPort.getNicknamesByIds(Set.of(auctionItem.getSellerId())).getOrDefault(auctionItem.getSellerId(), null);
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

}
