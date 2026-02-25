package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.port.out.UserPort;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.auction.event.AuctionItemUpdatedEvent;
import com.fourtune.shared.auction.event.AuctionStartedEvent;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.shared.kafka.auction.AuctionEventType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuctionStartUseCase {

    private static final String AGGREGATE_TYPE_AUCTION = "Auction";

    private final AuctionSupport auctionSupport;
    private final EventPublisher eventPublisher;
    private final UserPort userPort;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

    /**
     * 시작 시각이 지났는데 아직 SCHEDULED인 경매를 ACTIVE로 전환.
     * 상세 조회 시 호출해 스케줄러 대기 없이 바로 진행 중으로 표시.
     * (호출자 트랜잭션에 참여해 통합 테스트에서 저장 데이터가 보이도록 함)
     */
    @Transactional(readOnly = true)
    public void tryStartIfScheduledTimePassed(Long auctionId) {
        AuctionItem auction = auctionSupport.findByIdOrThrow(auctionId);
        if (auction.getStatus() != AuctionStatus.SCHEDULED || auction.getAuctionStartTime() == null) {
            return;
        }
        LocalDateTime nowKst = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        if (nowKst.isBefore(auction.getAuctionStartTime())) {
            return;
        }
        startAuctionInTransaction(auctionId);
    }

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
        String sellerName = userPort.getNicknamesByIds(Set.of(auction.getSellerId())).getOrDefault(auction.getSellerId(), null);
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
