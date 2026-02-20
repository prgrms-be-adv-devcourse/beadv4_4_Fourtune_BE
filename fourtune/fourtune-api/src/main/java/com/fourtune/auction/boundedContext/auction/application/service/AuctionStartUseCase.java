package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.boundedContext.user.application.service.UserFacade;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.shared.auction.event.AuctionStartedEvent;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.kafka.auction.AuctionEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuctionStartUseCase {

    private static final String AGGREGATE_TYPE_AUCTION = "Auction";

    private final AuctionSupport auctionSupport;
    private final EventPublisher eventPublisher;
    private final UserFacade userFacade;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void startAuctionInTransaction(Long auctionId) {
        AuctionItem auction = auctionSupport.findByIdOrThrow(auctionId);
        auction.start();
        auctionSupport.save(auction);
        Long aggregateId = auction.getId();

        // 경매 시작 이벤트 발행 (관심상품 알림용)
        AuctionStartedEvent startedEvent = new AuctionStartedEvent(
                auction.getId(),
                auction.getTitle(),
                auction.getSellerId(),
                auction.getStartPrice(),
                auction.getAuctionEndTime());
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, aggregateId, AuctionEventType.AUCTION_STARTED.name(), Map.of("eventType", AuctionEventType.AUCTION_STARTED.name(), "aggregateId", aggregateId, "data", startedEvent));
        } else {
            eventPublisher.publish(startedEvent);
        }

        // Search 인덱싱 전용 이벤트 발행 (스냅샷 형태)
        String thumbnailUrl = extractThumbnailUrl(auction);
        String sellerName = userFacade.getNicknamesByIds(Set.of(auction.getSellerId())).getOrDefault(auction.getSellerId(), null);
        AuctionItemUpdatedEvent itemUpdatedEvent = new AuctionItemUpdatedEvent(
                auction.getId(),
                auction.getSellerId(),
                sellerName,
                auction.getTitle(),
                auction.getDescription(),
                auction.getCategory().toString(),
                auction.getStatus().toString(),
                auction.getStartPrice(),
                auction.getCurrentPrice(),
                auction.getBuyNowPrice(),
                auction.getBuyNowEnabled(),
                auction.getAuctionStartTime(),
                auction.getAuctionEndTime(),
                thumbnailUrl,
                auction.getCreatedAt(),
                auction.getUpdatedAt(),
                auction.getViewCount(),
                auction.getBidCount(),
                auction.getWatchlistCount());
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, aggregateId, AuctionEventType.AUCTION_ITEM_UPDATED.name(), Map.of("eventType", AuctionEventType.AUCTION_ITEM_UPDATED.name(), "aggregateId", aggregateId, "data", itemUpdatedEvent));
        } else {
            eventPublisher.publish(itemUpdatedEvent);
        }
    }

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
