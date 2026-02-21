package com.fourtune.recommendation.adapter.out.ai;

import com.fourtune.recommendation.adapter.in.web.dto.RecommendedItemResponse;
import com.fourtune.recommendation.application.port.out.RecommendationAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

// OpenAI를 이용한 클라우드 AI 추천 구현체.
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.ai.provider", havingValue = "openai")
public class OpenAiClient implements RecommendationAiClient {

    private final OpenAiChatModel chatModel;

    @Override
    public List<RecommendedItemResponse> recommend(Long userId, List<String> topCategories,
            List<RecommendedItemResponse> candidates) {
        log.info("[REC][AI] OpenAI를 이용한 추천 랭킹 조정 시도 (userId: {})", userId);

        // TODO: OpenAI API를 통한 정교한 개인화 추천 로직
        return candidates;
    }
}
