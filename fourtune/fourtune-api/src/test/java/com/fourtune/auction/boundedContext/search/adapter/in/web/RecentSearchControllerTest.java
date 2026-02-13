package com.fourtune.auction.boundedContext.search.adapter.in.web;

import com.fourtune.auction.boundedContext.search.application.service.RecentSearchService;
import com.fourtune.common.shared.auth.dto.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RecentSearchControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private RecentSearchController recentSearchController;

    @Mock
    private RecentSearchService recentSearchService;

    // Custom Argument Resolver for @AuthenticationPrincipal UserContext
    static class MockUserContextArgumentResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.getParameterType().equals(UserContext.class);
        }

        @Override
        public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
            return new UserContext(1L, "password", List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        }
    }

    @BeforeEach
    void setUp() {
        // Manually setup MockMvc with custom ArgumentResolver to inject UserContext
        this.mockMvc = MockMvcBuilders.standaloneSetup(recentSearchController)
                .setCustomArgumentResolvers(new MockUserContextArgumentResolver())
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    @DisplayName("최근 검색어 목록을 조회한다.")
    void getRecentKeywords() throws Exception {
        // given: 서비스가 최근 검색어 목록을 반환하도록 설정
        List<String> mockKeywords = List.of("keyword1", "keyword2");
        given(recentSearchService.getKeywords(1L)).willReturn(mockKeywords);

        // when & then: GET 요청 시 200 OK와 함께 리스트 내용 검증
        mockMvc.perform(get("/api/v1/search/recent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()) // 배열인지 확인
                .andExpect(jsonPath("$.length()").value(2)) // 개수 확인
                .andExpect(jsonPath("$[0]").value("keyword1")) // 첫 번째 값 확인
                .andExpect(jsonPath("$[1]").value("keyword2")); // 두 번째 값 확인
    }

    @Test
    @DisplayName("로그인하지 않은 사용자는 빈 목록을 반환한다.")
    void getRecentKeywords_NoAuth() throws Exception {
        // MockMvc with no argument resolver for this test instance specifically
        MockMvc noAuthMockMvc = MockMvcBuilders.standaloneSetup(recentSearchController)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        // when & then: 인증 정보 없이 요청 시 빈 리스트 반환
        noAuthMockMvc.perform(get("/api/v1/search/recent"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("최근 검색어를 삭제한다.")
    void removeRecentKeyword() throws Exception {
        // given: 삭제할 키워드 준비
        String keyword = "test";

        // when & then: DELETE 요청 시 204 No Content 반환 확인
        mockMvc.perform(delete("/api/v1/search/recent")
                        .param("keyword", keyword))
                .andDo(print())
                .andExpect(status().isNoContent());
    }
}
