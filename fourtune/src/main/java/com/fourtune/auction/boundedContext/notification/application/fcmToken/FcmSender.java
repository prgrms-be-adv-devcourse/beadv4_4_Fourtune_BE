package com.fourtune.auction.boundedContext.notification.application.fcmToken;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class FcmSender {

    private final FirebaseMessaging firebaseMessaging;

    public void sendMulticast(List<String> tokens, String title, String content) {
        if (tokens.isEmpty()) return;

        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(content)
                .build();

        MulticastMessage message = MulticastMessage.builder()
                .setNotification(notification)
                .addAllTokens(tokens)
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

            if (response.getFailureCount() > 0) {
                log.error("🚫 알림 전송 실패: {} 건", response.getFailureCount());
                response.getResponses().forEach(r -> {
                    if (!r.isSuccessful()) {
                        log.error("실패 원인: {}", r.getException().getMessage());
                    }
                });
            } else {
                log.info("✅ 알림 전송 성공: {} 건", response.getSuccessCount());
            }

        } catch (FirebaseMessagingException e) {
            log.error("❌ FCM 전송 중 치명적 에러: ", e);
        }
    }
}
