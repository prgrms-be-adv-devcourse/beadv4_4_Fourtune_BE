package com.fourtune.auction.boundedContext.auction.port.out;

import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.auction.boundedContext.auction.domain.entity.AuctionItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

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
    
    List<AuctionItem> findByAuctionEndTimeBeforeAndStatus(
        LocalDateTime endTime, 
        AuctionStatus status
    );
    
    List<AuctionItem> findBySellerId(Long sellerId);
    
    List<AuctionItem> findByStatus(AuctionStatus status);
    
    /**
     * 시작 시간이 지났고 SCHEDULED 상태인 경매 조회 (자동 시작용)
     */
    List<AuctionItem> findByAuctionStartTimeBeforeAndStatus(
        LocalDateTime startTime,
        AuctionStatus status
    );
}
