package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.global.common.BaseTimeEntity;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "auction_orders", indexes = {
    @Index(name = "idx_auction_orders_order_id", columnList = "order_id", unique = true),
    @Index(name = "idx_auction_orders_auction_id", columnList = "auction_id"),
    @Index(name = "idx_auction_orders_winner_id", columnList = "winner_id")
})
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {
    
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
    /**
     * 주문 ID (UUID)
     * 프론트와 결제 시스템 간 주문 식별자
     */
    @Column(name = "order_id", unique = true, nullable = false, length = 100)
    private String orderId;
    
    /**
     * 경매 ID
     */
    @Column(nullable = false)
    private Long auctionId;
    
    /**
     * 낙찰자 ID (구매자)
     */
    @Column(nullable = false)
    private Long winnerId;
    
    /**
     * 판매자 ID
     */
    @Column(nullable = false)
    private Long sellerId;
    
    /**
     * 낙찰가 (결제 금액)
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    
    /**
     * 주문명 (결제 시 표시)
     */
    @Column(nullable = false, length = 200)
    private String orderName;
    
    /**
     * 주문 상태
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;
    
    /**
     * 결제 완료 시간
     */
    private LocalDateTime paidAt;
    
    // ==================== 비즈니스 메서드 ====================
    
    /**
     * 주문 생성 정적 팩토리 메서드
     * 비즈니스 규칙 검증 후 엔티티 생성
     */
    public static Order create(
            Long auctionId,
            Long winnerId,
            Long sellerId,
            BigDecimal amount,
            String orderName
    ) {
        // 1. 필수 필드 검증
        validateRequired(auctionId, winnerId, sellerId, amount, orderName);
        
        // 2. 금액 검증
        validateAmount(amount);
        
        // 3. 주문 ID 생성 (UUID)
        String orderId = generateOrderId();
        
        // 4. 엔티티 생성
        return Order.builder()
                .orderId(orderId)
                .auctionId(auctionId)
                .winnerId(winnerId)
                .sellerId(sellerId)
                .amount(amount)
                .orderName(orderName)
                .status(OrderStatus.PENDING)
                .build();
    }
    
    /**
     * 결제 완료 처리 (PENDING → COMPLETED)
     */
    public void complete() {
        if (this.status != OrderStatus.PENDING) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_PROCESSED);
        }
        
        this.status = OrderStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }
    
    /**
     * 주문 취소 (PENDING → CANCELLED)
     * 결제 완료 상태에서는 취소 불가
     */
    public void cancel() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
        }
        if (this.status == OrderStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        
        this.status = OrderStatus.CANCELLED;
    }
    
    /**
     * 결제 대기 상태 확인
     */
    public boolean isPending() {
        return this.status == OrderStatus.PENDING;
    }
    
    /**
     * 결제 완료 상태 확인
     */
    public boolean isCompleted() {
        return this.status == OrderStatus.COMPLETED;
    }
    
    /**
     * 취소 상태 확인
     */
    public boolean isCancelled() {
        return this.status == OrderStatus.CANCELLED;
    }
    
    // ==================== 검증 메서드 (private) ====================
    
    /**
     * 필수 필드 검증
     */
    private static void validateRequired(
            Long auctionId,
            Long winnerId,
            Long sellerId,
            BigDecimal amount,
            String orderName
    ) {
        if (auctionId == null || auctionId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (winnerId == null || winnerId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (sellerId == null || sellerId <= 0) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (amount == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        if (orderName == null || orderName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
    
    /**
     * 금액 검증
     * 0원 이하 주문 불가
     */
    private static void validateAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException(ErrorCode.ORDER_INVALID_AMOUNT);
        }
    }
    
    /**
     * 주문 ID 생성
     * UUID 기반 고유 주문 번호
     */
    private static String generateOrderId() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }
}
