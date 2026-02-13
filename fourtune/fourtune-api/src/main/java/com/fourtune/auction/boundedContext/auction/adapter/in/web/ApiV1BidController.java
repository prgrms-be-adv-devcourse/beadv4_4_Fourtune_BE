package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.boundedContext.auction.application.service.BidFacade;
import com.fourtune.common.global.common.ApiResponse;
import com.fourtune.common.shared.auction.dto.BidDetailResponse;
import com.fourtune.common.shared.auction.dto.BidHistoryResponse;
import com.fourtune.common.shared.auction.dto.BidPlaceRequest;
import com.fourtune.common.shared.auction.dto.BidResponse;
import com.fourtune.common.shared.auth.dto.UserContext;
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
        BidDetailResponse response = bidFacade.placeBid(
                request.auctionId(), 
                user.id(), 
                request.bidAmount()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
    
    /**
     * 입찰 취소
     */
    @DeleteMapping("/{bidId}")
    public ResponseEntity<Void> cancelBid(
        @AuthenticationPrincipal UserContext user,
        @PathVariable Long bidId
    ) {
        bidFacade.cancelBid(bidId, user.id());
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 경매의 입찰 내역 조회
     */
    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<BidHistoryResponse>> getAuctionBids(
        @PathVariable Long auctionId
    ) {
        BidHistoryResponse response = bidFacade.getAuctionBids(auctionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 사용자의 입찰 내역 조회
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getMyBids(
        @AuthenticationPrincipal UserContext user
    ) {
        List<BidResponse> response = bidFacade.getUserBids(user.id());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 경매의 최고가 입찰 조회
     */
    @GetMapping("/auction/{auctionId}/highest")
    public ResponseEntity<ApiResponse<BidResponse>> getHighestBid(
        @PathVariable Long auctionId
    ) {
        BidResponse response = bidFacade.getHighestBid(auctionId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 입찰 상세 조회
     */
    @GetMapping("/{bidId}")
    public ResponseEntity<ApiResponse<BidDetailResponse>> getBidDetail(
        @PathVariable Long bidId
    ) {
        BidDetailResponse response = bidFacade.getBidDetail(bidId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
