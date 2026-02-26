package com.fourtune.auction.boundedContext.auction.port.out;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {
    
    Page<AuctionItem> findByStatusAndCategory(
        AuctionStatus status, 
        Category category, 
        Pageable pageable
    );
    
    Page<AuctionItem> findByStatus(
        AuctionStatus status, 
        Pageable pageable
    );
    
    Page<AuctionItem> findBySellerIdOrderByCreatedAtDesc(
        Long sellerId,
        Pageable pageable
    );

    Page<AuctionItem> findBySellerIdAndStatusOrderByCreatedAtDesc(
        Long sellerId,
        AuctionStatus status,
        Pageable pageable
    );
    
    List<AuctionItem> findByAuctionEndTimeBeforeAndStatus(
        LocalDateTime endTime, 
        AuctionStatus status
    );
    
    List<AuctionItem> findBySellerId(Long sellerId);
    
    /**
     * 판매자별·상태별 경매 개수 (탈퇴 시 진행 중 경매 확인용)
     */
    long countBySellerIdAndStatus(Long sellerId, AuctionStatus status);
    
    List<AuctionItem> findByStatus(AuctionStatus status);
    
    /**
     * 시작 시간이 지났고 SCHEDULED 상태인 경매 조회 (자동 시작용)
     */
    List<AuctionItem> findByAuctionStartTimeLessThanEqualAndStatus(
        LocalDateTime startTime,
        AuctionStatus status
    );

    List<AuctionItem> findByAuctionStartTimeBetweenAndStatus(
            LocalDateTime from,
            LocalDateTime to,
            AuctionStatus status
    );

    /**
     * 종료 시간이 특정 범위에 해당하는 경매 조회 (관심상품 종료 알림용)
     */
    List<AuctionItem> findByAuctionEndTimeBetweenAndStatus(
            LocalDateTime from,
            LocalDateTime to,
            AuctionStatus status
    );

    /**
     * ID로 경매 조회 (Pessimistic Lock)
     * 입찰 시 동시성 제어를 위한 락
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AuctionItem a WHERE a.id = :id")
    Optional<AuctionItem> findByIdWithLock(@Param("id") Long id);

    /**
     * 조회수 벌크 증가 (Redis 동기화용)
     */
    @Modifying
    @Query("UPDATE AuctionItem a SET a.viewCount = a.viewCount + :delta WHERE a.id = :id")
    int addViewCount(@Param("id") Long id, @Param("delta") long delta);
}
