package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.api.infrastructure.kafka.notification.NotificationKafkaProducer;
import com.fourtune.api.infrastructure.kafka.search.SearchKafkaProducer;
import com.fourtune.api.infrastructure.kafka.watchList.WatchListKafkaProducer;
import com.fourtune.auction.boundedContext.auth.application.service.AuthService;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.auth.dto.TokenResponse;
import com.fourtune.shared.user.dto.UserLoginRequest;
import com.fourtune.shared.user.dto.UserSignUpRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@Transactional
class UserFacadeTest {

    @MockitoBean
    private com.google.firebase.messaging.FirebaseMessaging firebaseMessaging;

    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.in.event.AuctionItemIndexEventListener auctionItemIndexEventListener;

    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.ElasticsearchAuctionItemIndexingHandler elasticsearchAuctionItemIndexingHandler;

    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.repository.SearchAuctionItemCrudRepository searchAuctionItemCrudRepository;

    @MockitoBean
    private WatchListKafkaProducer watchListKafkaProducer;

    @MockitoBean
    private NotificationKafkaProducer notificationKafkaProducer;

    @MockitoBean
    private SearchKafkaProducer searchKafkaProducer;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("회원가입 시 유저 정보가 DB에 저장되고 비밀번호는 암호화되어야 한다")
    void signupSuccessTest() {
        // 1. Given: 가입 요청 데이터 준비
        UserSignUpRequest request = new UserSignUpRequest(
                "test1@example.com",
                "password123!",
                "테스터",
                "010-1234-5679"
        );

        // 2. When: 회원가입 실행
        userFacade.signup(request);

        // 3. Then: 검증
        User savedUser = userRepository.findByEmail("test1@example.com")
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        assertThat(savedUser.getEmail()).isEqualTo("test1@example.com");
        assertThat(savedUser.getNickname()).isEqualTo("테스터");

        // 비밀번호가 평문으로 저장되지 않고 암호화되었는지 확인
        assertThat(passwordEncoder.matches("password123!", savedUser.getPassword())).isTrue();
        assertThat(savedUser.getPassword()).isNotEqualTo("password123!");
    }

    @Test
    @DisplayName("이미 존재하는 이메일로 가입하면 예외가 발생해야 한다")
    void signupDuplicateEmailTest() {
        // Given: 이미 한 명이 가입된 상태
        UserSignUpRequest request = new UserSignUpRequest("test2@example.com", "password123!", "테스터", "010-5453-3461");
        userFacade.signup(request);

        UserSignUpRequest request2 = new UserSignUpRequest(
                "test2@example.com",
                "password123!",
                "테스터2",
                "010-3534-3246" // B 번호 (다르게 설정!)
        );
        // When & Then: 동일한 이메일로 가입 시도 시 예외 발생 검증
        assertThatThrownBy(() -> userFacade.signup(request2))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.EMAIL_DUPLICATION);
    }

    @Test
    @DisplayName("가입된 정보로 로그인하면 액세스 토큰과 리프레시 토큰이 발급된다")
    void loginSuccessTest() {
        // 1. Given: 유저가 이미 가입되어 있어야 함
        UserSignUpRequest signupRequest = new UserSignUpRequest(
                "login@test.com", "password123!", "로그인테스터", "010-1364-1367"
        );
        userFacade.signup(signupRequest);

        // 2. When: 로그인 시도
        UserLoginRequest loginRequest = new UserLoginRequest("login@test.com", "password123!");
        TokenResponse response = authService.login(loginRequest);

        // 3. Then: 검증
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
    }

}
