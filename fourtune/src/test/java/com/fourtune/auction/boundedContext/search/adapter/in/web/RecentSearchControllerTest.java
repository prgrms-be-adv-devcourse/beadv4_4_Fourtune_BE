package com.fourtune.auction.boundedContext.search.adapter.in.web;

import com.fourtune.auction.boundedContext.search.application.service.RecentSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class RecentSearchControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private RecentSearchService recentSearchService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    @DisplayName("최근 검색어 목록을 조회한다.")
    @WithMockUser
    void getRecentKeywords() throws Exception {
        // given: 서비스가 최근 검색어 목록을 반환하도록 설정
        given(recentSearchService.getKeywords(null)).willReturn(List.of("keyword1", "keyword2")); 

        // when & then: GET 요청 시 200 OK와 함께 정상 응답 확인
        mockMvc.perform(get("/api/v1/search/recent")
                        .with(csrf())) // CSRF 토큰 포함
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("최근 검색어를 삭제한다.")
    @WithMockUser
    void removeRecentKeyword() throws Exception {
        // given: 삭제할 키워드 준비
        String keyword = "test";

        // when & then: DELETE 요청 시 204 No Content 반환 확인
        mockMvc.perform(delete("/api/v1/search/recent")
                        .param("keyword", keyword)
                        .with(csrf())) // CSRF 토큰 포함
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
