package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.port.out.UserPort;
import com.fourtune.common.global.config.EventPublishingConfig;
import com.fourtune.common.global.eventPublisher.EventPublisher;
import com.fourtune.common.global.outbox.service.OutboxService;
import com.fourtune.common.shared.auction.dto.AuctionItemCreateRequest;
import com.fourtune.common.shared.auction.event.AuctionCreatedEvent;
import com.fourtune.common.shared.auction.event.AuctionItemCreatedEvent;
import com.fourtune.common.shared.auction.kafka.AuctionEventType;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 경매 생성 UseCase
 * - 경매 등록
 * - 이미지 업로드
 * - 경매 생성 이벤트 발행
 */
@Service
@RequiredArgsConstructor
public class AuctionCreateUseCase {

    private static final String AGGREGATE_TYPE_AUCTION = "Auction";

    private final AuctionSupport auctionSupport;
    private final EventPublisher eventPublisher;
    private final UserPort userPort;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;
    // private final S3Service s3Service; // TODO: 나중에 추가

    /**
     * 경매 등록
     */
    @Transactional
    public Long createAuction(Long sellerId, AuctionItemCreateRequest request) {
        // 1. 경매 엔티티 생성 (정적 팩토리 메서드 사용)
        AuctionItem auctionItem = AuctionItem.create(
                sellerId,
                request.title(),
                request.description(),
                request.category(),
                request.startPrice(),
                request.bidUnit() != null ? request.bidUnit() : 1000,
                request.buyNowPrice(),
                request.buyNowPrice() != null,  // buyNowEnabled
                request.auctionStartTime(),
                request.auctionEndTime()
        );
        
        // 2. DB 저장
        AuctionItem savedAuction = auctionSupport.save(auctionItem);
        Long aggregateId = savedAuction.getId();

        // 3. 이벤트 발행 (Kafka 사용 시 Outbox, 아니면 Spring Event)
        AuctionCreatedEvent createdEvent = new AuctionCreatedEvent(
                savedAuction.getId(),
                sellerId,
                savedAuction.getCategory().toString()
        );
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, aggregateId, AuctionEventType.AUCTION_CREATED.name(), Map.of("eventType", AuctionEventType.AUCTION_CREATED.name(), "aggregateId", aggregateId, "data", createdEvent));
        } else {
            eventPublisher.publish(createdEvent);
        }

        // 4. Search 인덱싱 전용 이벤트 발행 (스냅샷 형태)
        String thumbnailUrl = extractThumbnailUrl(savedAuction);
        String sellerName = userPort.getNicknamesByIds(Set.of(sellerId)).getOrDefault(sellerId, null);
        AuctionItemCreatedEvent itemCreatedEvent = new AuctionItemCreatedEvent(
                savedAuction.getId(),
                sellerId,
                sellerName,
                savedAuction.getTitle(),
                savedAuction.getDescription(),
                savedAuction.getCategory().toString(),
                savedAuction.getStatus().toString(),
                savedAuction.getStartPrice(),
                savedAuction.getCurrentPrice(),
                savedAuction.getBuyNowPrice(),
                savedAuction.getBuyNowEnabled(),
                savedAuction.getAuctionStartTime(),
                savedAuction.getAuctionEndTime(),
                thumbnailUrl,
                savedAuction.getCreatedAt(),
                savedAuction.getUpdatedAt(),
                savedAuction.getViewCount(),
                savedAuction.getBidCount(),
                savedAuction.getWatchlistCount()
        );
        if (eventPublishingConfig.isAuctionEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE_AUCTION, aggregateId, AuctionEventType.AUCTION_ITEM_CREATED.name(), Map.of("eventType", AuctionEventType.AUCTION_ITEM_CREATED.name(), "aggregateId", aggregateId, "data", itemCreatedEvent));
        } else {
            eventPublisher.publish(itemCreatedEvent);
        }

        // 5. 경매 ID 반환
        return savedAuction.getId();
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
