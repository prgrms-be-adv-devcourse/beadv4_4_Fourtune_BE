package com.fourtune.auction.boundedContext.watchList.adapter.web;

import com.fourtune.auction.boundedContext.watchList.application.service.WatchListService;
import com.fourtune.auction.shared.auth.dto.UserContext;
import com.fourtune.auction.shared.watchList.dto.WatchListRequestDto;
import com.fourtune.auction.shared.watchList.dto.WatchListResponseDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/watch-lists")
public class WatchListController {

    private final WatchListService watchListService;

    @PostMapping("toggle")
    public ResponseEntity<String> toggleWatchList(
            @AuthenticationPrincipal UserContext userContext,
            @Valid @RequestBody WatchListRequestDto request
    ) {
        boolean isAdded = watchListService.toggleWatchList(userContext.id(), request.auctionItemId());

        if (isAdded) {
            return ResponseEntity.ok("관심상품에 등록되었습니다.");
        } else {
            return ResponseEntity.ok("관심상품이 해제되었습니다.");
        }
    }

    @GetMapping
    public ResponseEntity<List<WatchListResponseDto>> getMyWatchLists(
            @AuthenticationPrincipal UserContext userContext
    ) {
        List<WatchListResponseDto> responses = watchListService.getMyWatchLists(userContext.id());
        return ResponseEntity.ok(responses);
    }

}
