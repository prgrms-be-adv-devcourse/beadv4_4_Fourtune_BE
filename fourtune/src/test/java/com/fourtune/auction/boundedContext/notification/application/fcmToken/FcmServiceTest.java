package com.fourtune.auction.boundedContext.notification.application.fcmToken;

import com.fourtune.auction.boundedContext.fcmToken.application.FcmSender;
import com.fourtune.auction.boundedContext.fcmToken.application.FcmService;
import com.fourtune.auction.boundedContext.fcmToken.port.out.FcmTokenRepository;
import com.fourtune.auction.boundedContext.notification.domain.NotificationSettings;
import com.fourtune.auction.boundedContext.notification.port.out.NotificationSettingsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    @Test
    @DisplayName("유저 알림 발송 시나리오 테스트")
    void fullScenarioTest() {
        // 1. Given (준비)
        Long fakeUserId = 1L;
        String fakeToken = "fake-device-token-123";

        NotificationSettings mockSettings = NotificationSettings.builder()
                //.user(mockUser)
                .build();

        // [Stubbing 1] 설정 조회 시 리턴값
        given(notificationSettingsRepository.findByUserId(fakeUserId))
                .willReturn(Optional.of(mockSettings));

        // [Stubbing 2] 토큰 조회 시 리턴값 (List<String> 반환해야 함!)
        given(fcmTokenRepository.findAllTokensByUserId(fakeUserId))
                .willReturn(List.of(fakeToken));

        // 2. When (실행)
        // (saveToken은 테스트 대상 아님, 바로 send 호출)
        fcmService.sendNotification(
                fakeUserId,
                "OUTBID", // isAllowed에서 BID_RECEIVED나 OUTBID 등 케이스에 맞아야 함
                "⚡ 상위 입찰 발생!",
                "메시지 내용"
        );

        // 3. Then (검증)
        // "FcmService야, 너 fcmSender한테 'sendMulticast' 하라고 시켰니?"
        verify(fcmSender).sendMulticast(any(), any(), any());
    }
}
