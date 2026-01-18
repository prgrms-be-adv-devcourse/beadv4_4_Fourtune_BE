package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * 경매 공통 조회/검증 기능
 * - Repository 직접 호출
 * - 여러 UseCase에서 재사용되는 공통 로직
 */
@Component
@RequiredArgsConstructor
public class AuctionSupport {

    private final AuctionItemRepository auctionItemRepository;

    /**
     * ID로 경매 조회 (Optional)
     */
    public Optional<AuctionItem> findById(Long auctionId) {
        // TODO: 구현 필요
        return Optional.empty();
    }

    /**
     * ID로 경매 조회 (예외 발생)
     */
    public AuctionItem findByIdOrThrow(Long auctionId) {
        // TODO: 구현 필요
        // return auctionItemRepository.findById(auctionId)
        //         .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
        return null;
    }

    /**
     * 경매 저장
     */
    public AuctionItem save(AuctionItem auctionItem) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 판매자 ID로 경매 목록 조회
     */
    public List<AuctionItem> findBySellerId(Long sellerId) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 상태별 경매 목록 조회 (페이징)
     */
    public Page<AuctionItem> findByStatus(AuctionStatus status, Pageable pageable) {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 진행중인 경매 목록 조회
     */
    public List<AuctionItem> findActiveAuctions() {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 종료 예정 경매 목록 조회 (자동 연장 체크용)
     */
    public List<AuctionItem> findEndingSoonAuctions() {
        // TODO: 구현 필요
        return null;
    }

    /**
     * 경매 삭제 가능 여부 검증
     */
    public void validateDeletable(AuctionItem auctionItem) {
        // TODO: 구현 필요
        // 입찰이 있으면 삭제 불가
        // 진행중인 경매는 삭제 불가
    }

    /**
     * 판매자 권한 검증
     */
    public void validateSeller(AuctionItem auctionItem, Long userId) {
        // TODO: 구현 필요
        // if (!auctionItem.getSellerId().equals(userId)) {
        //     throw new BusinessException(ErrorCode.FORBIDDEN);
        // }
    }

}
