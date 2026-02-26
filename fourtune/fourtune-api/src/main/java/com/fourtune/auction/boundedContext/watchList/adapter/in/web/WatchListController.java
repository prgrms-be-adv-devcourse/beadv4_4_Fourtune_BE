package com.fourtune.auction.boundedContext.watchList.adapter.in.web;

import com.fourtune.auction.boundedContext.watchList.application.service.WatchListService;
import com.fourtune.shared.auth.dto.UserContext;
import com.fourtune.shared.watchList.dto.WatchListRequestDto;
import com.fourtune.shared.watchList.dto.WatchListResponseDto;
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
    public ResponseEntity<Boolean> toggleWatchList(
            @AuthenticationPrincipal UserContext userContext,
            @Valid @RequestBody WatchListRequestDto request
    ) {
        boolean isAdded = watchListService.toggleWatchList(userContext.id(), request.auctionItemId());

        return ResponseEntity.ok(isAdded);
    }

    @GetMapping
    public ResponseEntity<List<WatchListResponseDto>> getMyWatchLists(
            @AuthenticationPrincipal UserContext userContext
    ) {
        List<WatchListResponseDto> responses = watchListService.getMyWatchLists(userContext.id());
        return ResponseEntity.ok(responses);
    }

}
