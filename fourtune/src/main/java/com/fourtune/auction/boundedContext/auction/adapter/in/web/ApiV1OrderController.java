package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.boundedContext.auction.application.service.OrderCompleteUseCase;
import com.fourtune.auction.boundedContext.auction.application.service.OrderQueryUseCase;
import com.fourtune.auction.global.common.ApiResponse;
import com.fourtune.auction.shared.auction.dto.OrderDetailResponse;
import com.fourtune.auction.shared.auction.dto.OrderResponse;
import com.fourtune.auction.shared.auth.dto.UserContext;
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
     * 주문번호로 주문 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderByOrderId(
        @AuthenticationPrincipal UserContext user,
        @PathVariable String orderId
    ) {
        // TODO: 구현 필요
        // 1. orderQueryUseCase.getOrderByOrderId(orderId) 호출
        // 2. 본인 확인 (winnerId == user.id())
        return null;
    }
    
    /**
     * 사용자의 주문 목록 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
        @AuthenticationPrincipal UserContext user
    ) {
        // TODO: 구현 필요
        // 1. orderQueryUseCase.getUserOrders(user.id()) 호출
        return null;
    }
    
    /**
     * 경매의 주문 조회
     */
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<OrderDetailResponse>> getOrderByAuctionId(
        @AuthenticationPrincipal UserContext user,
        @PathVariable Long auctionId
    ) {
        // TODO: 구현 필요
        // 1. orderQueryUseCase.getOrderByAuctionId(auctionId) 호출
        // 2. 권한 확인 (판매자 또는 낙찰자만)
        return null;
    }
    
    /**
     * 결제 완료 처리 (Payment 도메인에서 호출)
     */
    @PostMapping("/{orderId}/complete")
    public ResponseEntity<Void> completePayment(
        @PathVariable String orderId,
        @RequestParam String paymentKey
    ) {
        // TODO: 구현 필요
        // 1. orderCompleteUseCase.completePayment(orderId, paymentKey) 호출
        // 2. 내부 API이므로 인증 처리 필요 (API Key or 내부 통신)
        return null;
    }
}
