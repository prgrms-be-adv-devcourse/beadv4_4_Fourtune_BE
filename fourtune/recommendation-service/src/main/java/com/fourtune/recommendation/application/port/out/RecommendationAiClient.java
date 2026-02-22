package com.fourtune.recommendation.application.port.out;

import com.fourtune.recommendation.adapter.in.web.dto.RecommendedItemResponse;
import java.util.List;

/**
 * AI 추천 엔진 인터페이스 (전략 패턴).
 * 구현체에 따라 Ollama(로컬), OpenAI(클라우드), 또는 Rule-based로 동작합니다.
 */
public interface RecommendationAiClient {
    /**
     * 추천된 상품 리스트를 AI를 통해 재정렬하거나 추천 사유를 생성합니다.
     */
    List<RecommendedItemResponse> recommend(Long userId, List<String> topCategories,
            List<RecommendedItemResponse> candidates);
}
