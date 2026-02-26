package com.fourtune.auction.boundedContext.auction.port.out;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * orderId(UUID)로 주문 조회
     */
    Optional<Order> findByOrderId(String orderId);
    
    /**
     * 경매 ID로 주문 조회 (취소되지 않은 가장 최근 주문)
     * CANCELLED 이후 재주문이 허용되므로 여러 주문이 존재할 수 있음 → 활성 주문 우선 반환
     */
    Optional<Order> findFirstByAuctionIdAndStatusNotOrderByCreatedAtDesc(Long auctionId, OrderStatus status);

    /**
     * @deprecated 여러 주문 존재 시 NonUniqueResultException 발생 → findFirstByAuctionIdAndStatusNotOrderByCreatedAtDesc 사용
     */
    @Deprecated
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
     * 만료된 즉시구매 PENDING 주문 조회 (orderName이 "[즉시구매]"로 시작)
     * ORDER_PAYMENT_POLICY: 즉시구매 10분 유예
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :dateTime " +
            "AND o.orderName LIKE CONCAT(:buyNowPrefix, '%')")
    List<Order> findExpiredPendingBuyNowOrders(
            @Param("status") OrderStatus status,
            @Param("dateTime") LocalDateTime dateTime,
            @Param("buyNowPrefix") String buyNowPrefix
    );

    /**
     * 만료된 낙찰 PENDING 주문 조회 (orderName이 "[즉시구매]"로 시작하지 않음)
     * ORDER_PAYMENT_POLICY: 낙찰 24시간 유예
     */
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :dateTime " +
            "AND (o.orderName NOT LIKE CONCAT(:buyNowPrefix, '%') OR o.orderName IS NULL)")
    List<Order> findExpiredPendingBidOrders(
            @Param("status") OrderStatus status,
            @Param("dateTime") LocalDateTime dateTime,
            @Param("buyNowPrefix") String buyNowPrefix
    );
    
    /**
     * 경매에 주문이 이미 존재하는지 확인 (모든 상태 포함)
     */
    boolean existsByAuctionId(Long auctionId);

    /**
     * 경매에 활성 주문(PENDING/COMPLETED)이 존재하는지 확인
     * CANCELLED 주문은 제외 - 취소 후 재주문 허용을 위해
     */
    boolean existsByAuctionIdAndStatusNot(Long auctionId, OrderStatus status);
    
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

    /**
     * 경매당 유저당 취소된 즉시구매 주문 개수
     * orderName이 "[즉시구매]"로 시작하는 CANCELLED 주문만 카운트 (ORDER_PAYMENT_POLICY 이중 제한용)
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.auctionId = :auctionId AND o.winnerId = :winnerId " +
            "AND o.status = :status AND o.orderName LIKE :orderNamePrefix")
    long countByAuctionIdAndWinnerIdAndStatusAndOrderNameStartingWith(
            @Param("auctionId") Long auctionId,
            @Param("winnerId") Long winnerId,
            @Param("status") OrderStatus status,
            @Param("orderNamePrefix") String orderNamePrefix
    );
}
