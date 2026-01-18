package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 경매 삭제 UseCase
 * - 경매 삭제 (soft delete or hard delete)
 * - 입찰이 있으면 삭제 불가
 */
@Service
@RequiredArgsConstructor
public class AuctionDeleteUseCase {

    private final AuctionSupport auctionSupport;
    private final BidSupport bidSupport;
    private final AuctionItemRepository auctionItemRepository;

    /**
     * 경매 삭제
     */
    @Transactional
    public void deleteAuction(Long auctionId, Long userId) {
        // TODO: 구현 필요
        // 1. 경매 조회
        // 2. 판매자 권한 확인
        // 3. 삭제 가능 여부 확인 (입찰이 있으면 삭제 불가)
        // 4. 삭제 처리 (soft delete: status = CANCELLED or hard delete)
    }

    /**
     * 삭제 가능 여부 검증
     */
    private void validateDeletable(AuctionItem auctionItem) {
        // TODO: 구현 필요
        // - 입찰이 있으면 삭제 불가
        // - ACTIVE 상태면 삭제 불가 (먼저 취소 처리 필요)
    }

}
