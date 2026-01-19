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
        // TODO: 구현 필요
        // 1. 요청 검증 (필수 필드, 시작가 > 0, 종료시간 > 시작시간)
        // 2. 이미지 업로드 (S3)
        // 3. AuctionItem 엔티티 생성
        // 4. DB 저장
        // 5. 이벤트 발행 (AuctionCreatedEvent)
        // 6. 경매 ID 반환
        return null;
    }

    /**
     * 경매 생성 요청 검증
     */
    private void validateCreateRequest(AuctionItemCreateRequest request) {
        // TODO: 구현 필요
        // - title 필수
        // - startPrice > 0
        // - auctionEndTime > auctionStartTime
        // - buyNowPrice가 있으면 startPrice보다 커야 함
    }

}
