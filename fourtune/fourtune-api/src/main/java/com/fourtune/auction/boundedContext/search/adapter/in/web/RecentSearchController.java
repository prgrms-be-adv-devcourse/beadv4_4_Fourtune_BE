package com.fourtune.auction.boundedContext.search.adapter.in.web;

import com.fourtune.auction.boundedContext.search.application.service.RecentSearchService;
import com.fourtune.core.dto.ApiResponse;
import com.fourtune.shared.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/recent")
@RequiredArgsConstructor
public class RecentSearchController {

    private final RecentSearchService recentSearchService;

    // 최근 검색어 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<String>>> getRecentKeywords(@AuthenticationPrincipal UserContext user) {
        if (user == null) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }
        List<String> keywords = recentSearchService.getKeywords(user.id());
        return ResponseEntity.ok(ApiResponse.success(keywords));
    }

    // 최근 검색어 삭제
    @DeleteMapping
    public ResponseEntity<Void> removeRecentKeyword(
            @AuthenticationPrincipal UserContext user,
            @RequestParam String keyword
    ) {
        if (user != null) {
            recentSearchService.removeKeyword(user.id(), keyword);
        }
        return ResponseEntity.noContent().build();
    }
}
