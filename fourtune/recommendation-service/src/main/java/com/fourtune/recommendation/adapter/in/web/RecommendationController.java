package com.fourtune.recommendation.adapter.in.web;

import com.fourtune.recommendation.adapter.in.web.dto.RecommendedItemResponse;
import com.fourtune.recommendation.application.service.RecommendationService;
import com.fourtune.recommendation.common.RecommendationConstants;
import com.fourtune.shared.auth.dto.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 추천 API 컨트롤러.
 *
 * <pre>
 * GET /api/v1/recommendations          — 인증 사용자 개인화 추천 (JWT 필수)
 * GET /api/v1/recommendations/popular   — 비로그인 인기 추천
 * </pre>
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 인증된 사용자 기반 개인화 추천.
     * Redis 프로파일의 상위 카테고리로 Search API를 호출합니다.
     */
    @GetMapping
    public ResponseEntity<List<RecommendedItemResponse>> getRecommendations(
            @AuthenticationPrincipal UserContext user,
            @RequestParam(defaultValue = "10") int size) {
        int effectiveSize = Math.min(Math.max(size, 1), RecommendationConstants.MAX_SIZE);

        List<RecommendedItemResponse> items = recommendationService.getRecommendations(user.id(), effectiveSize);
        return ResponseEntity.ok(items);
    }

    /**
     * 비로그인 사용자용 인기 상품 추천.
     * viewCount 기반 인기순으로 정렬된 ACTIVE 상품을 반환합니다.
     */
    @GetMapping("/popular")
    public ResponseEntity<List<RecommendedItemResponse>> getPopularItems(
            @RequestParam(defaultValue = "10") int size) {
        int effectiveSize = Math.min(Math.max(size, 1), 50);

        List<RecommendedItemResponse> items = recommendationService.getPopularItems(effectiveSize);
        return ResponseEntity.ok(items);
    }
}
