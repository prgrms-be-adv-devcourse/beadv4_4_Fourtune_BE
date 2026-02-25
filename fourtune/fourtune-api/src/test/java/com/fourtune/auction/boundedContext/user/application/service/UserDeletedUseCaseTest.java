package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.adapter.out.external.AuctionServiceClient;
import com.fourtune.auction.boundedContext.user.adapter.out.external.dto.ActiveAuctionsResponse;
import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.outbox.service.OutboxService;
import com.fourtune.shared.user.dto.UserWithdrawRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserDeletedUseCaseTest {

    @Mock
    private UserSupport userSupport;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private EventPublishingConfig eventPublishingConfig;
    @Mock
    private OutboxService outboxService;
    @Mock
    private AuctionServiceClient auctionServiceClient;

    @InjectMocks
    private UserDeletedUseCase userDeletedUseCase;

    private static final Long USER_ID = 1L;
    private static final String RAW_PASSWORD = "password123!";
    private static final String ENCODED_PASSWORD = "$2a$10$encodedPassword";

    private User activeUser;
    private UserWithdrawRequest request;

    @BeforeEach
    void setUp() {
        activeUser = User.builder()
                .email("test@example.com")
                .nickname("테스터")
                .password(ENCODED_PASSWORD)
                .status(Status.ACTIVE)
                .build();

        request = new UserWithdrawRequest(RAW_PASSWORD, null);
    }

    @Test
    @DisplayName("진행 중 경매가 없고 비밀번호가 일치하면 탈퇴에 성공한다")
    void userDelete_success() {
        // given
        given(userSupport.findByIdOrThrow(USER_ID)).willReturn(activeUser);
        given(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(auctionServiceClient.getActiveAuctionsByUser(any()))
                .willReturn(new ActiveAuctionsResponse(false, 0));
        given(eventPublishingConfig.isUserEventsKafkaEnabled()).willReturn(false);

        // when
        userDeletedUseCase.userDelete(USER_ID, request);

        // then
        assertThat(activeUser.getStatus()).isEqualTo(Status.SUSPENDED);
        assertThat(activeUser.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 탈퇴한 유저는 ALREADY_WITHDRAWN 예외가 발생한다")
    void userDelete_alreadyWithdrawn_throwsException() {
        // given
        User suspendedUser = User.builder()
                .email("withdrawn@example.com")
                .nickname("탈퇴자")
                .password(ENCODED_PASSWORD)
                .status(Status.SUSPENDED)
                .build();
        given(userSupport.findByIdOrThrow(USER_ID)).willReturn(suspendedUser);

        // when & then
        assertThatThrownBy(() -> userDeletedUseCase.userDelete(USER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_WITHDRAWN);

        verify(auctionServiceClient, never()).getActiveAuctionsByUser(any());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 PASSWORD_NOT_MATCH 예외가 발생한다")
    void userDelete_wrongPassword_throwsException() {
        // given
        given(userSupport.findByIdOrThrow(USER_ID)).willReturn(activeUser);
        given(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> userDeletedUseCase.userDelete(USER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PASSWORD_NOT_MATCH);

        verify(auctionServiceClient, never()).getActiveAuctionsByUser(any());
    }

    @Test
    @DisplayName("진행 중인 경매가 있으면 WITHDRAW_BLOCKED_BY_ACTIVE_AUCTION 예외가 발생한다")
    void userDelete_hasActiveAuctions_throwsException() {
        // given
        given(userSupport.findByIdOrThrow(USER_ID)).willReturn(activeUser);
        given(passwordEncoder.matches(RAW_PASSWORD, ENCODED_PASSWORD)).willReturn(true);
        given(auctionServiceClient.getActiveAuctionsByUser(any()))
                .willReturn(new ActiveAuctionsResponse(true, 2));

        // when & then
        assertThatThrownBy(() -> userDeletedUseCase.userDelete(USER_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.WITHDRAW_BLOCKED_BY_ACTIVE_AUCTION);

        assertThat(activeUser.getStatus()).isEqualTo(Status.ACTIVE);
    }
}
