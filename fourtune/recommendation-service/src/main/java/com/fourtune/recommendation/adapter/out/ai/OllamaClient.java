package com.fourtune.recommendation.adapter.out.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.recommendation.adapter.in.web.dto.RecommendedItemResponse;
import com.fourtune.recommendation.application.port.out.RecommendationAiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ollama(로컬 AI)를 이용한 추천 구현체.
 * 로컬/개발 환경에서 사용. feature.ai.provider=ollama 일 때 활성화.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.ai.provider", havingValue = "ollama")
public class OllamaClient implements RecommendationAiClient {

    private final OllamaChatModel chatModel;
    private final ObjectMapper objectMapper;

    @Override
    public List<RecommendedItemResponse> recommend(Long userId, List<String> topCategories,
            List<RecommendedItemResponse> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        log.info("[REC][AI] Ollama 추천 시작: userId={}, 후보={}개, 카테고리={}",
                userId, candidates.size(), topCategories);

        try {
            String prompt = buildPrompt(userId, topCategories, candidates);
            String response = chatModel.call(prompt);
            return parseResponse(response, candidates);
        } catch (Exception e) {
            log.error("[REC][AI] Ollama 호출 실패, 원본 순서 반환: {}", e.getMessage());
            return candidates;
        }
    }

    private String buildPrompt(Long userId, List<String> topCategories,
            List<RecommendedItemResponse> candidates) {
        String itemList = candidates.stream()
                .map(item -> String.format("ID:%d | %s | %s | 현재가:%s | 입찰:%d | 찜:%d | 조회:%d",
                        item.auctionItemId(), item.title(), item.category(),
                        item.currentPrice(), item.bidCount(), item.watchlistCount(), item.viewCount()))
                .collect(Collectors.joining("\n"));

        return """
                경매 플랫폼 추천 시스템입니다.

                [사용자 선호 카테고리 (가중 점수 순)]
                %s

                [현재 ACTIVE 경매 상품 목록]
                %s

                위 상품 중 이 사용자에게 가장 적합한 상품을 추천 우선순위가 높은 순서대로 정렬하세요.
                선호 카테고리와의 관련성, 입찰 활발도, 인기도를 종합적으로 고려하세요.

                반드시 아래 JSON 배열 형식으로만 응답하세요. 다른 텍스트 없이 JSON만 출력:
                [ID1, ID2, ID3, ...]
                """.formatted(
                String.join(", ", topCategories),
                itemList);
    }

    private List<RecommendedItemResponse> parseResponse(String response,
            List<RecommendedItemResponse> candidates) {
        try {
            // AI 응답에서 JSON 배열 추출 (앞뒤 텍스트 제거)
            String json = response.trim();
            int start = json.indexOf('[');
            int end = json.lastIndexOf(']');
            if (start == -1 || end == -1) {
                log.warn("[REC][AI] JSON 배열 파싱 실패, 원본 반환: {}", response);
                return candidates;
            }
            json = json.substring(start, end + 1);

            List<Long> rankedIds = objectMapper.readValue(json, new TypeReference<List<Long>>() {});

            // ID 순서대로 candidates를 재정렬
            List<RecommendedItemResponse> ranked = rankedIds.stream()
                    .map(id -> candidates.stream()
                            .filter(c -> c.auctionItemId().equals(id))
                            .findFirst()
                            .orElse(null))
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());

            // AI가 누락한 항목은 뒤에 추가
            candidates.stream()
                    .filter(c -> !rankedIds.contains(c.auctionItemId()))
                    .forEach(ranked::add);

            log.info("[REC][AI] Ollama 추천 완료: 재정렬 {}개", ranked.size());
            return ranked;

        } catch (Exception e) {
            log.error("[REC][AI] 응답 파싱 실패, 원본 반환: {}", e.getMessage());
            return candidates;
        }
    }
}
