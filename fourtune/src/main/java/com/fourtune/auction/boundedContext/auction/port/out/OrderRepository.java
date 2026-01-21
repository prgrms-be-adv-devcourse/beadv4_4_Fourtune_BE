package com.fourtune.auction.boundedContext.auction.port.out;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * orderId(UUID)로 주문 조회
     */
    Optional<Order> findByOrderId(String orderId);
    
    /**
     * 경매 ID로 주문 조회
     */
    Optional<Order> findByAuctionId(Long auctionId);
    
    /**
     * 낙찰자(구매자) ID로 주문 목록 조회
     */
    List<Order> findByWinnerIdOrderByCreatedAtDesc(Long winnerId);
    
    /**
     * 낙찰자 주문 목록 조회 (페이징)
     */
    Page<Order> findByWinnerId(Long winnerId, Pageable pageable);
    
    /**
     * 판매자 ID로 주문 목록 조회
     */
    List<Order> findBySellerIdOrderByCreatedAtDesc(Long sellerId);
    
    /**
     * 판매자 주문 목록 조회 (페이징)
     */
    Page<Order> findBySellerId(Long sellerId, Pageable pageable);
    
    /**
     * 특정 상태의 주문 목록 조회
     */
    List<Order> findByStatus(OrderStatus status);
    
    /**
     * 낙찰자의 특정 상태 주문 목록
     */
    List<Order> findByWinnerIdAndStatus(Long winnerId, OrderStatus status);
    
    /**
     * 판매자의 특정 상태 주문 목록
     */
    List<Order> findBySellerIdAndStatus(Long sellerId, OrderStatus status);
    
    /**
     * 결제 대기 중인 주문 중 일정 시간 경과한 주문 조회
     */
    List<Order> findByStatusAndCreatedAtBefore(
        OrderStatus status, 
        LocalDateTime dateTime
    );
    
    /**
     * 경매에 주문이 이미 존재하는지 확인
     */
    boolean existsByAuctionId(Long auctionId);
    
    /**
     * orderId 존재 여부 확인
     */
    boolean existsByOrderId(String orderId);
    
    /**
     * 낙찰자의 주문 개수
     */
    long countByWinnerId(Long winnerId);
    
    /**
     * 판매자의 주문 개수
     */
    long countBySellerId(Long sellerId);
    
    /**
     * 특정 상태의 주문 개수
     */
    long countByStatus(OrderStatus status);
}
