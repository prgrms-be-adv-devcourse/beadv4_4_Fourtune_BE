package com.fourtune.auction.boundedContext.auth.adapter.in;

import com.fourtune.auction.boundedContext.auth.application.service.AuthService;
import com.fourtune.auction.shared.auth.dto.ReissueRequest;
import com.fourtune.auction.shared.auth.dto.TokenResponse;
import com.fourtune.auction.shared.user.dto.UserLoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .build();
    }

    @Test
    void login_Success() throws Exception {
        UserLoginRequest request = new UserLoginRequest("test@email.com", "pw");
        TokenResponse response = new TokenResponse("grant", "acc", "ref");

        given(authService.login(any())).willReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("재발급 API 성공 테스트")
    void reissue_Api_Success() throws Exception {
        String refreshToken = "valid_refresh_token";
        ReissueRequest request = new ReissueRequest(refreshToken);
        TokenResponse response = new TokenResponse("Bearer", "new_access", "new_refresh");

        given(authService.reissue(refreshToken)).willReturn(response);

        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new_access"))
                .andExpect(jsonPath("$.refreshToken").value("new_refresh"));
    }

}
