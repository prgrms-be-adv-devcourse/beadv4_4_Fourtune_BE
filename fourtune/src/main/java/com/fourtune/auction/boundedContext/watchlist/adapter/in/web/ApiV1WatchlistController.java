package com.fourtune.auction.boundedContext.watchlist.adapter.in.web;

import com.fourtune.auction.boundedContext.watchlist.application.service.WatchlistFacade;
import com.fourtune.auction.global.common.ApiResponse;
import com.fourtune.auction.shared.watchlist.dto.WatchlistAddRequest;
import com.fourtune.auction.shared.watchlist.dto.WatchlistResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관심상품 REST API Controller
 */
@Tag(name = "Watchlist", description = "관심상품 API")
@RestController
@RequestMapping("/api/v1/watchlist")
@RequiredArgsConstructor
public class ApiV1WatchlistController {

    private final WatchlistFacade watchlistFacade;

    @Operation(summary = "관심상품 추가", description = "경매를 관심상품에 추가합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> addToWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody WatchlistAddRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Long watchlistId = watchlistFacade.addToWatchlist(userId, request.auctionId());
        return ResponseEntity.ok(ApiResponse.success(watchlistId, "관심상품에 추가되었습니다."));
    }

    @Operation(summary = "관심상품 제거", description = "관심상품에서 제거합니다.")
    @DeleteMapping("/{watchlistId}")
    public ResponseEntity<ApiResponse<Void>> removeFromWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long watchlistId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        watchlistFacade.removeFromWatchlist(watchlistId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "관심상품에서 제거되었습니다."));
    }

    @Operation(summary = "관심상품 제거 (경매 ID로)", description = "경매 ID로 관심상품에서 제거합니다.")
    @DeleteMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<Void>> removeByAuctionId(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long auctionId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        watchlistFacade.removeByAuctionId(userId, auctionId);
        return ResponseEntity.ok(ApiResponse.success(null, "관심상품에서 제거되었습니다."));
    }

    @Operation(summary = "관심상품 목록 조회", description = "내 관심상품 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WatchlistResponse>>> getWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Page<WatchlistResponse> watchlist = watchlistFacade.getWatchlist(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(watchlist, "관심상품 목록 조회 성공"));
    }

    @Operation(summary = "관심상품 전체 목록 조회", description = "내 관심상품 전체 목록을 조회합니다.")
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<WatchlistResponse>>> getAllWatchlist(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<WatchlistResponse> watchlist = watchlistFacade.getAllWatchlist(userId);
        return ResponseEntity.ok(ApiResponse.success(watchlist, "관심상품 전체 목록 조회 성공"));
    }

    @Operation(summary = "관심상품 개수 조회", description = "내 관심상품 개수를 조회합니다.")
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getWatchlistCount(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        long count = watchlistFacade.getWatchlistCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "관심상품 개수 조회 성공"));
    }

    @Operation(summary = "관심상품 등록 여부 확인", description = "특정 경매가 관심상품에 등록되어 있는지 확인합니다.")
    @GetMapping("/check/{auctionId}")
    public ResponseEntity<ApiResponse<Boolean>> isInWatchlist(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long auctionId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        boolean isInWatchlist = watchlistFacade.isInWatchlist(userId, auctionId);
        return ResponseEntity.ok(ApiResponse.success(isInWatchlist, "관심상품 등록 여부 확인 성공"));
    }

}
