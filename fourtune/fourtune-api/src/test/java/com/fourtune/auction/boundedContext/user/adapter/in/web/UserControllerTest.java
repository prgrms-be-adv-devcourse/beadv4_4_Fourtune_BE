package com.fourtune.auction.boundedContext.user.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fourtune.auction.boundedContext.notification.adapter.in.kafka.NotificationUserKafkaListener;
import com.fourtune.auction.boundedContext.settlement.adapter.in.kafka.SettlementUserKafkaListener;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.fourtune.auction.boundedContext.watchList.adapter.in.kafka.WatchListUserKafkaListener;
import com.fourtune.common.shared.user.dto.UserResponse;
import com.fourtune.common.shared.user.dto.UserSignUpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
public class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @MockitoBean
    private com.google.firebase.messaging.FirebaseMessaging firebaseMessaging;

    @MockitoBean private WatchListUserKafkaListener watchListUserKafkaListener;
    @MockitoBean private SettlementUserKafkaListener settlementUserKafkaListener;
    @MockitoBean private NotificationUserKafkaListener notificationUserKafkaListener;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
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

    @Test
    @DisplayName("GET /api/users/{id} - 가입된 유저 ID로 조회 시 id, email, nickname이 반환된다")
    void getUserByIdTest() throws Exception {
        UserSignUpRequest signup = new UserSignUpRequest(
                "public@example.com", "Password123!", "공개닉네임", "010-1111-2222"
        );
        mockMvc.perform(post("/api/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signup)))
                .andExpect(status().isCreated());

        Long userId = userRepository.findByEmail("public@example.com").orElseThrow().getId();
        var result = mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andReturn();
        String json = result.getResponse().getContentAsString();
        UserResponse parsed = objectMapper.readValue(json, UserResponse.class);
        assertThat(parsed.id()).isEqualTo(userId);
        assertThat(parsed.email()).isEqualTo("public@example.com");
        assertThat(parsed.nickname()).isEqualTo("공개닉네임");
    }
}
