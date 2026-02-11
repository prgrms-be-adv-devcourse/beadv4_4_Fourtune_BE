package com.fourtune.auction.boundedContext.auth.application.service.oauth;

import com.fourtune.auction.boundedContext.user.application.service.UserSupport;
import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.eventPublisher.EventPublisher; // 추가됨
import com.fourtune.auction.shared.auth.dto.UserContext;
import com.fourtune.auction.shared.user.event.UserJoinedEvent; // 추가됨
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
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomOAuth2UserServiceTest {

    @InjectMocks
    private CustomOAuth2UserService customOAuth2UserService;

    @Mock
    private UserSupport userSupport;

    @Mock // 1. EventPublisher 추가 (서비스에 주입되므로 필요)
    private EventPublisher eventPublisher;

    @Test
    @DisplayName("소셜 로그인 정보를 받으면(신규 유저) 저장을 하고 이벤트를 발행한 뒤 UserContext를 반환한다")
    void processUserTest() {
        // Given
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

        // 2. Mocking 수정: findOrCreate가 아니라 findByEmail + save를 정의해야 함

        // (1) 이메일로 찾았을 때 -> 없다(Empty)고 설정 (신규 가입 시나리오)
        given(userSupport.findByEmail(anyString())).willReturn(Optional.empty());

        // (2) 저장(save)을 호출하면 -> fakeUser를 리턴해라 (여기가 핵심! 이거 없어서 Null 떴음)
        given(userSupport.save(any(User.class))).willReturn(fakeUser);

        // When
        OAuth2User result = customOAuth2UserService.processUser(fakeOAuth2User, provider);

        // Then
        assertThat(result).isInstanceOf(UserContext.class);
        UserContext userContext = (UserContext) result;
        assertThat(userContext.id()).isEqualTo(1L);

        // 3. 검증: findByEmail과 save가 호출되었는지 확인
        verify(userSupport).findByEmail(email);
        verify(userSupport).save(any(User.class));

        // 4. 검증: 이벤트가 잘 발행되었는지 확인
        verify(eventPublisher).publish(any(UserJoinedEvent.class));
    }
}
