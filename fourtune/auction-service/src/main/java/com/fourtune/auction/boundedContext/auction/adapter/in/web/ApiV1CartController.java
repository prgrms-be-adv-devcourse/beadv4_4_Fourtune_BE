package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.boundedContext.auction.application.service.CartFacade;
import com.fourtune.core.dto.ApiResponse;
import com.fourtune.shared.auction.dto.CartAddItemRequest;
import com.fourtune.shared.auction.dto.CartBuyNowRequest;
import com.fourtune.shared.auction.dto.CartResponse;
import com.fourtune.shared.auth.dto.UserContext;
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
        CartResponse response = cartFacade.getUserCart(user.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 장바구니에 아이템 추가
     */
    @PostMapping("/items")
    public ResponseEntity<Void> addItemToCart(
        @AuthenticationPrincipal UserContext user,
        @RequestBody @Valid CartAddItemRequest request
    ) {
        cartFacade.addItemToCart(user.id(), request.auctionId());
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
        cartFacade.removeItemFromCart(user.id(), cartItemId);
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
        List<String> orderIds = cartFacade.buyNowFromCart(user.id(), request.cartItemIds());
        return ResponseEntity.ok(ApiResponse.success(orderIds));
    }
    
    /**
     * 장바구니 전체 즉시구매
     */
    @PostMapping("/buy-now/all")
    public ResponseEntity<ApiResponse<List<String>>> buyNowAllCart(
        @AuthenticationPrincipal UserContext user
    ) {
        List<String> orderIds = cartFacade.buyNowAllCart(user.id());
        return ResponseEntity.ok(ApiResponse.success(orderIds));
    }
    
    /**
     * 만료된 아이템 제거
     */
    @DeleteMapping("/expired")
    public ResponseEntity<Void> clearExpiredItems(
        @AuthenticationPrincipal UserContext user
    ) {
        cartFacade.clearExpiredItems(user.id());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 장바구니 아이템 개수 조회
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Integer>> getCartItemCount(
        @AuthenticationPrincipal UserContext user
    ) {
        int count = cartFacade.getActiveItemCount(user.id());
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
