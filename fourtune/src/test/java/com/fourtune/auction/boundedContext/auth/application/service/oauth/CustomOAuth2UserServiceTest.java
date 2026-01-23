package com.fourtune.auction.boundedContext.auth.application.service.oauth;

import com.fourtune.auction.boundedContext.user.application.service.UserSupport;
import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.shared.auth.dto.UserContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private UserSupport userSupport;

    @Test
    @DisplayName("소셜 로그인 정보를 받으면 UserSupport를 통해 유저를 저장하고 UserContext를 반환한다")
    void processUserTest() {
        String email = "test@example.com";
        String name = "테스트유저";
        String provider = "google";
        String providerId = "123456789";

        Map<String, Object> attributes = Map.of(
                "email", email,
                "name", name,
                "sub", providerId
        );
        OAuth2User fakeOAuth2User = new DefaultOAuth2User(
                Collections.emptyList(),
                attributes,
                "sub"
        );

        User fakeUser = User.builder()
                .id(1L)
                .email(email)
                .role(Role.USER)
                .build();

        given(userSupport.findOrCreate(anyString(), anyString(), anyString(), anyString()))
                .willReturn(fakeUser);

        OAuth2User result = customOAuth2UserService.processUser(fakeOAuth2User, provider);

        assertThat(result).isInstanceOf(UserContext.class);

        UserContext userContext = (UserContext) result;
        assertThat(userContext.email()).isEqualTo(email);

        verify(userSupport).findOrCreate(email, name, provider, providerId);
    }
}
