package com.fourtune.auction.boundedContext.notification.application.fcmToken;

import com.fourtune.auction.boundedContext.notification.domain.NotificationSettings;
import com.fourtune.auction.boundedContext.notification.port.out.NotificationSettingsRepository;
import com.fourtune.auction.boundedContext.notification.port.out.fcmToken.FcmTokenRepository;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional // 테스트 끝나면 DB 깔끔하게 롤백 (데이터 남기지 않음)
class FcmServiceTest {

    @Autowired private FcmService fcmService;
    @Autowired private UserRepository userRepository;
    @Autowired private NotificationSettingsRepository settingsRepository;

    @Test
    @DisplayName("시나리오: 유저 가입부터 알림 수신까지 한 방에 테스트")
    @Rollback(false)
    void fullScenarioTest() {

        User user = User.builder()
                .email("tester@example.com")
                .nickname("테스트유저")
                .password("1234")
                .build();
        userRepository.save(user);

        Long userId = user.getId();
        System.out.println("✅ 1. 유저 생성 완료: ID = " + userId);

        NotificationSettings settings = NotificationSettings.builder()
                .user(user)
                .build();
        settingsRepository.save(settings);
        System.out.println("✅ 2. 알림 설정 저장 완료: 입찰알림 ON");

        String realDeviceToken = "e2mt4BVO82pSXeCv7qX9yL:APA91bEgCC3-_USXG0__ABF5TIyV44XfeWzXNrWszXHN-eZVhnkWWtcbNKbZtV-mvnDGQ_1STdRoO6Bc9oMYXY91Nh8beNGpXky0r60Kv_L3tAAa1GQ-P60";

        fcmService.saveToken(userId, realDeviceToken);

        System.out.println("✅ 3. FCM 토큰 저장 완료");
        System.out.println("🚀 4. 알림 발송 시작...");

        fcmService.sendNotification(
                userId,
                "OUTBID",
                "⚡ 상위 입찰 발생!",
                "회원님이 입찰한 물품에 더 높은 가격이 제시되었습니다."
        );

        System.out.println("🏁 5. 테스트 종료 (콘솔 로그와 핸드폰을 확인하세요)");
    }
}
