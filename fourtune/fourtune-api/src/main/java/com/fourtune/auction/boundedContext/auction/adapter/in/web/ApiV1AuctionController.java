package com.fourtune.auction.boundedContext.auction.adapter.in.web;

import com.fourtune.auction.boundedContext.auction.application.service.AuctionFacade;
import com.fourtune.auction.boundedContext.auction.domain.constant.AuctionStatus;
import com.fourtune.auction.boundedContext.auction.domain.constant.Category;
import com.fourtune.shared.auth.dto.UserContext;
import com.fourtune.shared.auction.dto.AuctionItemCreateRequest;
import com.fourtune.shared.auction.dto.AuctionItemDetailResponse;
import com.fourtune.shared.auction.dto.AuctionItemResponse;
import com.fourtune.shared.auction.dto.AuctionItemUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@RequiredArgsConstructor
public class ApiV1AuctionController {
    
    private final AuctionFacade auctionFacade;
    
    @PostMapping
    public ResponseEntity<AuctionItemResponse> createAuction(
        @AuthenticationPrincipal UserContext user,
        @RequestPart @Valid AuctionItemCreateRequest request,
        @RequestPart(required = false) List<MultipartFile> images
    ) {
        Long sellerId = user.id();
        AuctionItemResponse response = auctionFacade.createAuction(sellerId, request, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping
    public ResponseEntity<Page<AuctionItemResponse>> getAuctionList(
        @RequestParam(required = false) AuctionStatus status,
        @RequestParam(required = false) Category category,
        Pageable pageable
    ) {
        Page<AuctionItemResponse> response = auctionFacade.getAuctionList(
            status, 
            category, 
            pageable
        );
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<AuctionItemDetailResponse> getAuctionDetail(
        @PathVariable Long id
    ) {
        AuctionItemDetailResponse response = auctionFacade.getAuctionDetail(id);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<AuctionItemResponse> updateAuction(
        @AuthenticationPrincipal UserContext user,
        @PathVariable Long id,
        @RequestBody @Valid AuctionItemUpdateRequest request
    ) {
        Long userId = user.id();
        AuctionItemResponse response = auctionFacade.updateAuction(id, userId, request);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuction(
        @AuthenticationPrincipal UserContext user,
        @PathVariable Long id
    ) {
        Long userId = user.id();
        auctionFacade.deleteAuction(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PatchMapping("/{id}/view")
    public ResponseEntity<Void> increaseViewCount(@PathVariable Long id) {
        auctionFacade.increaseViewCount(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 즉시구매 처리
     * 경매 상세 페이지에서 "즉시구매" 버튼 클릭 시 호출
     */
    @PostMapping("/{id}/buy-now")
    public ResponseEntity<String> buyNow(
        @AuthenticationPrincipal UserContext user,
        @PathVariable Long id
    ) {
        Long buyerId = user.id();
        String orderId = auctionFacade.executeBuyNow(id, buyerId);
        return ResponseEntity.ok(orderId);
    }
}
