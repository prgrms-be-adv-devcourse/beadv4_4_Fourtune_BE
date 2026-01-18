package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.boundedContext.auction.application.service.CartFacade;
import com.fourtune.auction.global.common.ApiResponse;
import com.fourtune.auction.shared.auction.dto.CartAddItemRequest;
import com.fourtune.auction.shared.auction.dto.CartBuyNowRequest;
import com.fourtune.auction.shared.auction.dto.CartResponse;
import com.fourtune.auction.shared.auth.dto.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 장바구니 REST API Controller
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class ApiV1CartController {
    
    private final CartFacade cartFacade;
    
    /**
     * 장바구니 조회
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(
        @AuthenticationPrincipal UserContext user
    ) {
        // TODO: 구현 필요
        // 1. cartFacade.getUserCart(user.id()) 호출
        return null;
    }
    
    /**
     * 장바구니에 아이템 추가
     */
    @PostMapping("/items")
    public ResponseEntity<Void> addItemToCart(
        @AuthenticationPrincipal UserContext user,
        @RequestBody @Valid CartAddItemRequest request
    ) {
        // TODO: 구현 필요
        // 1. cartFacade.addItemToCart(user.id(), request.auctionId()) 호출
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    /**
     * 장바구니에서 아이템 제거
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<Void> removeItemFromCart(
        @AuthenticationPrincipal UserContext user,
        @PathVariable Long cartItemId
    ) {
        // TODO: 구현 필요
        // 1. cartFacade.removeItemFromCart(user.id(), cartItemId) 호출
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 장바구니에서 선택 아이템 즉시구매
     */
    @PostMapping("/buy-now")
    public ResponseEntity<ApiResponse<List<String>>> buyNowFromCart(
        @AuthenticationPrincipal UserContext user,
        @RequestBody @Valid CartBuyNowRequest request
    ) {
        // TODO: 구현 필요
        // 1. cartFacade.buyNowFromCart(user.id(), request.cartItemIds()) 호출
        // 2. 생성된 주문 ID 목록 반환
        return null;
    }
    
    /**
     * 장바구니 전체 즉시구매
     */
    @PostMapping("/buy-now/all")
    public ResponseEntity<ApiResponse<List<String>>> buyNowAllCart(
        @AuthenticationPrincipal UserContext user
    ) {
        // TODO: 구현 필요
        // 1. cartFacade.buyNowAllCart(user.id()) 호출
        // 2. 생성된 주문 ID 목록 반환
        return null;
    }
    
    /**
     * 만료된 아이템 제거
     */
    @DeleteMapping("/expired")
    public ResponseEntity<Void> clearExpiredItems(
        @AuthenticationPrincipal UserContext user
    ) {
        // TODO: 구현 필요
        // 1. cartFacade.clearExpiredItems(user.id()) 호출
        return ResponseEntity.noContent().build();
    }
}
