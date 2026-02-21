package com.fourtune.recommendation.adapter.out.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.recommendation.adapter.in.web.dto.RecommendedItemResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.ollama.OllamaChatModel;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OllamaClientTest {

    @Mock
    private OllamaChatModel chatModel;

    private OllamaClient ollamaClient;

    @BeforeEach
    void setUp() {
        ollamaClient = new OllamaClient(chatModel, new ObjectMapper());
    }

    @Test
    @DisplayName("AI가 JSON 배열로 정상 응답하면 재정렬됨")
    void recommend_validJsonResponse_reordersItems() {
        List<String> categories = List.of("전자기기", "패션");
        List<RecommendedItemResponse> candidates = List.of(
                createItem(1L, "상품A"),
                createItem(2L, "상품B"),
                createItem(3L, "상품C"));

        // AI가 2, 3, 1 순서로 추천
        when(chatModel.call(anyString())).thenReturn("[2, 3, 1]");

        List<RecommendedItemResponse> result = ollamaClient.recommend(1L, categories, candidates);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).auctionItemId()).isEqualTo(2L);
        assertThat(result.get(1).auctionItemId()).isEqualTo(3L);
        assertThat(result.get(2).auctionItemId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("AI 응답에 텍스트가 섞여있어도 JSON 배열 추출 성공")
    void recommend_jsonWithSurroundingText_parsesCorrectly() {
        List<RecommendedItemResponse> candidates = List.of(
                createItem(10L, "상품X"),
                createItem(20L, "상품Y"));

        when(chatModel.call(anyString())).thenReturn("Here is my recommendation:\n[20, 10]\nThank you.");

        List<RecommendedItemResponse> result = ollamaClient.recommend(1L, List.of("패션"), candidates);

        assertThat(result.get(0).auctionItemId()).isEqualTo(20L);
        assertThat(result.get(1).auctionItemId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("AI가 일부 ID만 반환하면 나머지는 뒤에 추가")
    void recommend_partialIds_appendsRemaining() {
        List<RecommendedItemResponse> candidates = List.of(
                createItem(1L, "A"), createItem(2L, "B"), createItem(3L, "C"));

        // AI가 2번만 추천
        when(chatModel.call(anyString())).thenReturn("[2]");

        List<RecommendedItemResponse> result = ollamaClient.recommend(1L, List.of("전자기기"), candidates);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).auctionItemId()).isEqualTo(2L); // AI 추천
        // 나머지 1, 3은 뒤에 추가 (순서 보장 안 하지만 존재 확인)
        assertThat(result).extracting(RecommendedItemResponse::auctionItemId)
                .containsExactlyInAnyOrder(2L, 1L, 3L);
    }

    @Test
    @DisplayName("AI 호출 실패 시 원본 순서 반환 (폴백)")
    void recommend_aiException_returnsCandidatesAsIs() {
        List<RecommendedItemResponse> candidates = List.of(
                createItem(1L, "A"), createItem(2L, "B"));

        when(chatModel.call(anyString())).thenThrow(new RuntimeException("Ollama connection refused"));

        List<RecommendedItemResponse> result = ollamaClient.recommend(1L, List.of("전자기기"), candidates);

        assertThat(result).isEqualTo(candidates);
    }

    @Test
    @DisplayName("AI가 잘못된 형식 반환 시 원본 순서 반환")
    void recommend_invalidFormat_returnsCandidatesAsIs() {
        List<RecommendedItemResponse> candidates = List.of(createItem(1L, "A"));

        when(chatModel.call(anyString())).thenReturn("I recommend item 1");

        List<RecommendedItemResponse> result = ollamaClient.recommend(1L, List.of("전자기기"), candidates);

        assertThat(result).isEqualTo(candidates);
    }

    @Test
    @DisplayName("빈 후보 리스트이면 빈 리스트 반환")
    void recommend_emptyCandidates_returnsEmpty() {
        List<RecommendedItemResponse> result = ollamaClient.recommend(
                1L, List.of("전자기기"), Collections.emptyList());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("null 후보 리스트이면 빈 리스트 반환")
    void recommend_nullCandidates_returnsEmpty() {
        List<RecommendedItemResponse> result = ollamaClient.recommend(
                1L, List.of("전자기기"), null);

        assertThat(result).isEmpty();
    }

    private RecommendedItemResponse createItem(Long id, String title) {
        return new RecommendedItemResponse(id, title, "전자기기", "ACTIVE",
                BigDecimal.valueOf(10000), BigDecimal.valueOf(50000), true,
                "thumb.jpg", LocalDateTime.now(), LocalDateTime.now().plusDays(7), 100, 10, 5);
    }
}
