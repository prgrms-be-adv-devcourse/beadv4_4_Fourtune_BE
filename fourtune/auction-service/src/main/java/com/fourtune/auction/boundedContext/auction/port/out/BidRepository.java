package com.fourtune.auction.boundedContext.auction.port.out;

import com.fourtune.auction.boundedContext.auction.domain.constant.BidStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BidRepository extends JpaRepository<Bid, Long> {
    
    /**
     * 경매별 입찰 목록 조회 (최신순)
     */
    List<Bid> findByAuctionIdOrderByCreatedAtDesc(Long auctionId);
    
    /**
     * 경매별 입찰 목록 조회 (입찰가 높은순)
     */
    List<Bid> findByAuctionIdOrderByBidAmountDesc(Long auctionId);
    
    /**
     * 경매별 입찰 목록 조회 (페이징)
     */
    Page<Bid> findByAuctionId(Long auctionId, Pageable pageable);
    
    /**
     * 사용자별 입찰 목록 조회 (최신순)
     */
    List<Bid> findByBidderIdOrderByCreatedAtDesc(Long bidderId);
    
    /**
     * 사용자별 입찰 목록 조회 (페이징)
     */
    Page<Bid> findByBidderId(Long bidderId, Pageable pageable);
    
    /**
     * 경매의 최고 입찰 조회
     */
    Optional<Bid> findTopByAuctionIdAndStatusOrderByBidAmountDesc(
        Long auctionId, 
        BidStatus status
    );
    
    /**
     * 경매의 낙찰된 입찰 조회 (isWinning = true)
     */
    Optional<Bid> findByAuctionIdAndIsWinningTrue(Long auctionId);
    
    /**
     * 경매별 특정 상태의 입찰 목록
     */
    List<Bid> findByAuctionIdAndStatus(Long auctionId, BidStatus status);
    
    /**
     * 사용자의 특정 경매 입찰 조회 (최신순)
     */
    Optional<Bid> findTopByAuctionIdAndBidderIdOrderByCreatedAtDesc(
        Long auctionId, 
        Long bidderId
    );
    
    /**
     * 경매의 입찰 개수
     */
    long countByAuctionId(Long auctionId);
    
    /**
     * 경매의 특정 상태 입찰 개수
     */
    long countByAuctionIdAndStatus(Long auctionId, BidStatus status);
    
    /**
     * 사용자가 특정 경매에 입찰했는지 확인
     */
    boolean existsByAuctionIdAndBidderId(Long auctionId, Long bidderId);
    
    /**
     * 경매의 모든 ACTIVE 입찰을 FAILED로 변경 (낙찰자 제외)
     */
    @Query("UPDATE Bid b SET b.status = 'FAILED' " +
           "WHERE b.auctionId = :auctionId " +
           "AND b.status = 'ACTIVE' " +
           "AND b.isWinning = false")
    void updateFailedBidsForAuction(@Param("auctionId") Long auctionId);
}
