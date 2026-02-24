package com.fourtune.auction.boundedContext.auction.application.service;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import com.fourtune.auction.boundedContext.auction.port.out.AuctionItemRepository;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 경매 공통 조회/검증 기능
 * - Repository 직접 호출
 * - 여러 UseCase에서 재사용되는 공통 로직
 */
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionSupport {

    private final AuctionItemRepository auctionItemRepository;

    /**
     * ID로 경매 조회 (Optional)
     */
    public Optional<AuctionItem> findById(Long auctionId) {
        return auctionItemRepository.findById(auctionId);
    }

    /**
     * ID로 경매 조회 (예외 발생)
     */
    public AuctionItem findByIdOrThrow(Long auctionId) {
        return auctionItemRepository.findById(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
    }

    /**
     * ID로 경매 조회 (Pessimistic Lock 적용)
     * 입찰 시 동시성 제어를 위해 사용
     */
    public AuctionItem findByIdWithLockOrThrow(Long auctionId) {
        return auctionItemRepository.findByIdWithLock(auctionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUCTION_NOT_FOUND));
    }

    /**
     * 경매 저장
     */
    public AuctionItem save(AuctionItem auctionItem) {
        return auctionItemRepository.save(auctionItem);
    }

    /**
     * 판매자 ID로 경매 목록 조회
     */
    public List<AuctionItem> findBySellerId(Long sellerId) {
        return auctionItemRepository.findBySellerId(sellerId);
    }

    /**
     * 상태, 카테고리별 경매 목록 조회 (페이징)
     */
    public Page<AuctionItem> findByStatusAndCategory(
            AuctionStatus status,
            com.fourtune.auction.boundedContext.auction.domain.constant.Category category,
            Pageable pageable) {
        return auctionItemRepository.findByStatusAndCategory(status, category, pageable);
    }

    /**
     * 상태별 경매 목록 조회 (페이징)
     */
    public Page<AuctionItem> findByStatus(AuctionStatus status, Pageable pageable) {
        return auctionItemRepository.findByStatus(status, pageable);
    }

    /**
     * 전체 경매 목록 조회 (페이징)
     */
    public Page<AuctionItem> findAll(Pageable pageable) {
        return auctionItemRepository.findAll(pageable);
    }

    /**
     * 판매자별 경매 목록 조회 (페이징)
     */
    public Page<AuctionItem> findBySellerIdPaged(Long sellerId, Pageable pageable) {
        return auctionItemRepository.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
    }

    /**
     * 판매자의 진행 중(ACTIVE) 경매 개수 (탈퇴 시 확인용)
     */
    public long countActiveBySellerId(Long sellerId) {
        return auctionItemRepository.countBySellerIdAndStatus(sellerId, AuctionStatus.ACTIVE);
    }

    /**
     * 진행중인 경매 목록 조회
     */
    public List<AuctionItem> findActiveAuctions() {
        return auctionItemRepository.findByStatus(AuctionStatus.ACTIVE);
    }

    /**
     * 종료 시간이 지난 경매 목록 조회
     */
    public List<AuctionItem> findExpiredAuctions(java.time.LocalDateTime now) {
        return auctionItemRepository.findByAuctionEndTimeBeforeAndStatus(
                now,
                AuctionStatus.ACTIVE);
    }

    /**
     * 시작 시간이 되었지만 아직 시작되지 않은 경매 목록 조회
     */
    public List<AuctionItem> findScheduledAuctionsToStart(java.time.LocalDateTime now) {
        return auctionItemRepository.findByAuctionStartTimeLessThanEqualAndStatus(
                now,
                AuctionStatus.SCHEDULED);
    }

    /**
     * 경매 삭제 가능 여부 검증
     * 입찰이 있거나 진행중인 경매는 삭제 불가
     */
    public void validateDeletable(AuctionItem auctionItem) {
        if (auctionItem.getBidCount() > 0) {
            throw new BusinessException(ErrorCode.AUCTION_HAS_BIDS);
        }
        if (auctionItem.getStatus() == AuctionStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.AUCTION_IN_PROGRESS);
        }
    }

    /**
     * 판매자 권한 검증
     */
    public void validateSeller(AuctionItem auctionItem, Long userId) {
        if (!auctionItem.getSellerId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 시작 5분 전 경매 조회 (SCHEDULED 상태, [now+4분, now+5분] 윈도우)
     */
    public List<AuctionItem> findAuctionsStartingInFiveMinutes(java.time.LocalDateTime now) {
        return auctionItemRepository.findByAuctionStartTimeBetweenAndStatus(
                now.plusMinutes(4),
                now.plusMinutes(5).minusSeconds(1),
                AuctionStatus.SCHEDULED);
    }

    /**
     * 종료 5분 전 경매 조회 (ACTIVE 상태, [now+4분, now+5분] 윈도우)
     */
    public List<AuctionItem> findAuctionsEndingInFiveMinutes(java.time.LocalDateTime now) {
        return auctionItemRepository.findByAuctionEndTimeBetweenAndStatus(
                now.plusMinutes(4),
                now.plusMinutes(5).minusSeconds(1),
                AuctionStatus.ACTIVE);
    }

}
