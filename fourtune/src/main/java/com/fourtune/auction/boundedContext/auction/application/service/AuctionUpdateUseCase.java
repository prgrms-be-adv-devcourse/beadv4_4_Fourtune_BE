package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.shared.auction.dto.AuctionItemUpdateRequest;
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

    private final AuctionSupport auctionSupport;
    private final BidSupport bidSupport;

    /**
     * 경매 정보 수정
     */
    @Transactional
    public void updateAuction(Long auctionId, Long userId, AuctionItemUpdateRequest request) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 판매자 권한 확인
        // 3. 수정 가능 여부 확인 (입찰이 있으면 일부만 수정 가능)
        // 4. 엔티티 수정
        // 5. DB 저장 (dirty checking)
    }

    /**
     * 수정 가능 여부 검증
     */
    private void validateUpdateable(AuctionItem auctionItem) {
        // TODO: 구현 필요
        // - SCHEDULED 상태: 모든 필드 수정 가능
        // - ACTIVE 상태: 
        //   - 입찰이 없으면 모든 필드 수정 가능
        //   - 입찰이 있으면 제목, 설명만 수정 가능
        // - ENDED, SOLD, CANCELLED: 수정 불가
    }

}
