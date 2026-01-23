package com.fourtune.auction.boundedContext.notification.application.fcmToken;

import com.fourtune.auction.boundedContext.fcmToken.application.FcmSender;
import com.fourtune.auction.boundedContext.fcmToken.application.FcmService;
import com.fourtune.auction.boundedContext.fcmToken.port.out.FcmTokenRepository;
import com.fourtune.auction.boundedContext.notification.domain.NotificationSettings;
import com.fourtune.auction.boundedContext.notification.port.out.NotificationSettingsRepository;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FcmServiceTest {
    @InjectMocks
    private FcmService fcmService;

    @Mock
    private NotificationSettingsRepository notificationSettingsRepository;

    @Mock
    private FcmSender fcmSender;

    @Mock
    private FcmTokenRepository fcmTokenRepository;

    @Mock
    private FirebaseMessaging firebaseMessaging;


    @Test
    @DisplayName("유저 알림 발송 시나리오 테스트 (Mock 사용)")
    void fullScenarioTest() throws FirebaseMessagingException {
        Long fakeUserId = 1L;
        String fakeToken = "fake-device-token-123";

        User mockUser = User.builder()
                .id(fakeUserId)
                .email("tester@example.com")
                .nickname("테스트유저")
                .build();

        NotificationSettings mockSettings = NotificationSettings.builder()
                .user(mockUser)
                .build();

        BatchResponse mockResponse = mock(BatchResponse.class);
        given(mockResponse.getSuccessCount()).willReturn(1);
        given(firebaseMessaging.sendEachForMulticast(any(MulticastMessage.class)))
                .willReturn(mockResponse);

        fcmService.saveToken(fakeUserId, fakeToken);

        fcmService.sendNotification(
                fakeUserId,
                "OUTBID",
                "⚡ 상위 입찰 발생!",
                "메시지 내용"
        );

        verify(firebaseMessaging).sendEachForMulticast(any(MulticastMessage.class));
    }
}
