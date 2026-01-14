package com.fourtune.auction.boundedContext.user.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.shared.user.dto.UserSignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper; // 수동 초기화

    @Autowired
    private WebApplicationContext context;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper(); // 직접 생성
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();
    }

    @Test
    @DisplayName("Swagger UI와 회원가입 API는 인증 없이 접근 가능하다")
    void publicPathTest() throws Exception {
        // 1. Swagger 데이터 경로 확인
        mockMvc.perform(get("/v3/api-docs"))
                .andDo(print())
                .andExpect(status().isOk());

        // 2. 회원가입 (정상 데이터) - post 메서드는 MockMvcRequestBuilders.post여야 합니다.
        UserSignUpRequest validRequest = new UserSignUpRequest(
                "test@example.com", "Password123!", "정상 닉네임", "010-1234-5678"
        );

        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("이메일 형식이 틀리면 400 에러와 함께 검증 메시지를 반환한다")
    void validationFailTest() throws Exception {
        // Given: 유효하지 않은 데이터
        UserSignUpRequest invalidRequest = new UserSignUpRequest(
                "wrong-email", "123", "01012345678", ""
        );

        // When & Then
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(result -> {
                    String content = result.getResponse().getContentAsString();
                    assertThat(content).isNotEmpty();
                    System.out.println("발생한 에러 메시지: " + content);
                });
    }
}
