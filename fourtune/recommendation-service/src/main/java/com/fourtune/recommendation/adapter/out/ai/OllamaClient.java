package com.fourtune.recommendation.adapter.out.ai;

import com.fourtune.recommendation.adapter.in.web.dto.RecommendedItemResponse;
import com.fourtune.recommendation.application.port.out.RecommendationAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.ai.provider", havingValue = "ollama")
public class OllamaClient implements RecommendationAiClient {

    private final OllamaChatModel chatModel;

    @Override
    public List<RecommendedItemResponse> recommend(Long userId, List<String> topCategories,
            List<RecommendedItemResponse> candidates) {
        log.info("[REC][AI] Ollama를 이용한 추천 랭킹 조정 시도 (userId: {})", userId);

        // TODO: 실제 AI 프롬프트 엔지니어링을 통한 상품 재정렬 로직 구현
        // 현재는 AI 모델 연결 확인 단계이므로 우선 순서 유지하여 반환
        return candidates;
    }
}
