package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auction.dto.AuctionItemCreateRequest;
import com.fourtune.auction.shared.auction.event.AuctionCreatedEvent;
import com.fourtune.auction.shared.auction.event.AuctionItemCreatedEvent;
import lombok.RequiredArgsConstructor;
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

    private final AuctionSupport auctionSupport;
    private final EventPublisher eventPublisher;
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
        
        // 3. 이벤트 발행
        eventPublisher.publish(new AuctionCreatedEvent(
                savedAuction.getId(),
                sellerId,
                savedAuction.getCategory()
        ));
        
        // 4. Search 인덱싱 전용 이벤트 발행 (스냅샷 형태)
        String thumbnailUrl = extractThumbnailUrl(savedAuction);
        eventPublisher.publish(new AuctionItemCreatedEvent(
                savedAuction.getId(),
                savedAuction.getTitle(),
                savedAuction.getDescription(),
                savedAuction.getCategory(),
                savedAuction.getStatus(),
                savedAuction.getStartPrice(),
                savedAuction.getCurrentPrice(),
                savedAuction.getAuctionStartTime(),
                savedAuction.getAuctionEndTime(),
                thumbnailUrl,
                savedAuction.getCreatedAt(),
                savedAuction.getUpdatedAt(),
                savedAuction.getViewCount(),
                savedAuction.getBidCount(),
                savedAuction.getWatchlistCount()
        ));
        
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
