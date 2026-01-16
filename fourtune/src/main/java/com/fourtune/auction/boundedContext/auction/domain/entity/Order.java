package com.fourtune.auction.boundedContext.auction.domain.entity;

import com.fourtune.auction.boundedContext.auction.domain.constant.OrderStatus;
import com.fourtune.auction.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    
    // 비즈니스 메서드
    
    /**
     * 결제 완료 처리
     */
    public void complete() {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("이미 처리된 주문입니다.");
        }
        this.status = OrderStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }
    
    /**
     * 주문 취소
     */
    public void cancel() {
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException("완료된 주문은 취소할 수 없습니다.");
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
}
