package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.auction.dto.AuctionItemCreateRequest;
import com.fourtune.auction.shared.auction.event.AuctionCreatedEvent;
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
        
        // 3. 이벤트 발행 (선택)
        eventPublisher.publish(new AuctionCreatedEvent(
                savedAuction.getId(),
                sellerId,
                savedAuction.getCategory()
        ));
        
        // 4. 경매 ID 반환
        return savedAuction.getId();
    }

}
