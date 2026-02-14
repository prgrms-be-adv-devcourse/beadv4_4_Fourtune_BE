package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.boundedContext.auction.application.service.OrderCompleteUseCase;
import com.fourtune.auction.boundedContext.auction.application.service.OrderQueryUseCase;
import com.fourtune.common.global.common.ApiResponse;
import com.fourtune.common.global.error.ErrorCode;
import com.fourtune.common.global.error.exception.BusinessException;
import com.fourtune.common.shared.auction.dto.OrderDetailResponse;
import com.fourtune.common.shared.auction.dto.OrderResponse;
import com.fourtune.common.shared.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 주문 REST API Controller
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class ApiV1OrderController {
    
    private final OrderQueryUseCase orderQueryUseCase;
    private final OrderCompleteUseCase orderCompleteUseCase;
    
    /**
     * 주문번호로 주문 조회 (인증 필요)
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderByOrderId(
        @AuthenticationPrincipal UserContext user,
        @PathVariable String orderId
    ) {
        OrderDetailResponse response = orderQueryUseCase.getOrderByOrderId(orderId);
        
        // 본인 확인 (구매자 또는 판매자)
        if (!response.winnerId().equals(user.id()) && !response.sellerId().equals(user.id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 주문번호로 주문 조회 (인증 없음 - 결제 페이지용)
     * orderId(UUID)로 OrderDetailResponse 조회
     */
    @GetMapping("/public/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderByOrderId(
            @PathVariable("orderId") String orderId
    ) {
        OrderDetailResponse response = orderQueryUseCase.getOrderByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 사용자의 주문 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
        @AuthenticationPrincipal UserContext user
    ) {
        List<OrderResponse> response = orderQueryUseCase.getUserOrders(user.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 경매의 주문 조회
     */
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderByAuctionId(
        @AuthenticationPrincipal UserContext user,
        @PathVariable Long auctionId
    ) {
        OrderDetailResponse response = orderQueryUseCase.getOrderByAuctionId(auctionId);
        
        // 권한 확인 (구매자 또는 판매자만)
        if (!response.winnerId().equals(user.id()) && !response.sellerId().equals(user.id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 결제 완료 처리 (Payment 도메인에서 호출)
     * 내부 API - 별도 인증 처리 필요
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<Void> completePayment(
        @PathVariable String orderId,
        @RequestParam String paymentKey
    ) {
        orderCompleteUseCase.completePayment(orderId, paymentKey);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 주문 취소
     */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(
        @AuthenticationPrincipal UserContext user,
        @PathVariable String orderId
    ) {
        // 주문 조회하여 본인 확인 후 취소
        OrderDetailResponse order = orderQueryUseCase.getOrderByOrderId(orderId);
        if (!order.winnerId().equals(user.id())) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        
        orderCompleteUseCase.cancelOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
