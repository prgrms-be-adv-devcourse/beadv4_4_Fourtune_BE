package com.fourtune.auction.boundedContext.watchlist.port.out;

import com.fourtune.auction.boundedContext.watchlist.domain.entity.Watchlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 관심상품 Repository
 */
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    /**
     * 사용자 + 경매 ID로 조회
     */
    Optional<Watchlist> findByUserIdAndAuctionId(Long userId, Long auctionId);

    /**
     * 사용자 + 경매 ID로 존재 여부 확인
     */
    boolean existsByUserIdAndAuctionId(Long userId, Long auctionId);

    /**
     * 사용자의 관심상품 목록 조회 (페이징)
     */
    Page<Watchlist> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자의 관심상품 전체 목록 조회
     */
    List<Watchlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 관심상품 개수 조회
     */
    long countByUserId(Long userId);

    /**
     * 특정 경매의 관심상품 등록자 목록 (알림 발송용)
     */
    List<Watchlist> findByAuctionId(Long auctionId);

    /**
     * 마감 임박 알림 대상 조회 (알림 설정 ON + 종료 시간 임박)
     */
    @Query("SELECT w FROM Watchlist w WHERE w.notifyOnEndingSoon = true " +
           "AND w.auctionEndTime BETWEEN :now AND :threshold")
    List<Watchlist> findEndingSoonWatchlists(
            @Param("now") LocalDateTime now, 
            @Param("threshold") LocalDateTime threshold);

    /**
     * 특정 경매의 가격 변동 알림 대상 조회
     */
    @Query("SELECT w FROM Watchlist w WHERE w.auctionId = :auctionId AND w.notifyOnPriceChange = true")
    List<Watchlist> findByAuctionIdAndNotifyOnPriceChangeTrue(@Param("auctionId") Long auctionId);

    /**
     * 사용자의 관심상품 전체 삭제
     */
    void deleteByUserId(Long userId);

    /**
     * 특정 경매 관련 관심상품 전체 삭제 (경매 종료 시)
     */
    void deleteByAuctionId(Long auctionId);

}
