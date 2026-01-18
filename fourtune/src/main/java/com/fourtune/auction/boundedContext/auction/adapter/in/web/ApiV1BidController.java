package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.boundedContext.auction.application.service.BidFacade;
import com.fourtune.auction.global.common.ApiResponse;
import com.fourtune.auction.shared.auction.dto.BidDetailResponse;
import com.fourtune.auction.shared.auction.dto.BidHistoryResponse;
import com.fourtune.auction.shared.auction.dto.BidPlaceRequest;
import com.fourtune.auction.shared.auction.dto.BidResponse;
import com.fourtune.auction.shared.auth.dto.UserContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 입찰 REST API Controller
 */
@RestController
@RequestMapping("/api/v1/bids")
@RequiredArgsConstructor
public class ApiV1BidController {
    
    private final BidFacade bidFacade;
    
    /**
     * 입찰하기
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BidDetailResponse>> placeBid(
        @AuthenticationPrincipal UserContext user,
        @RequestBody @Valid BidPlaceRequest request
    ) {
        // TODO: 구현 필요
        // 1. bidFacade.placeBid(request.auctionId(), user.id(), request.bidAmount()) 호출
        // 2. 응답 반환
        return null;
    }
    
    /**
     * 입찰 취소
     */
    @DeleteMapping("/{bidId}")
    public ResponseEntity<Void> cancelBid(
        @AuthenticationPrincipal UserContext user,
        @PathVariable Long bidId
    ) {
        // TODO: 구현 필요
        // 1. bidFacade.cancelBid(bidId, user.id()) 호출
        return null;
    }
    
    /**
     * 경매의 입찰 내역 조회
     */
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<BidHistoryResponse>> getAuctionBids(
        @PathVariable Long auctionId
    ) {
        // TODO: 구현 필요
        // 1. bidFacade.getAuctionBids(auctionId) 호출
        return null;
    }
    
    /**
     * 사용자의 입찰 내역 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getMyBids(
        @AuthenticationPrincipal UserContext user
    ) {
        // TODO: 구현 필요
        // 1. bidFacade.getUserBids(user.id()) 호출
        return null;
    }
    
    /**
     * 경매의 최고가 입찰 조회
     */
    @GetMapping("/auction/{auctionId}/highest")
    public ResponseEntity<ApiResponse<BidResponse>> getHighestBid(
        @PathVariable Long auctionId
    ) {
        // TODO: 구현 필요
        // 1. bidFacade.getHighestBid(auctionId) 호출
        return null;
    }
}
