package com.fourtune.auction.boundedContext.fcmToken.application;

import com.fourtune.auction.boundedContext.notification.domain.NotificationSettings;
import com.fourtune.auction.boundedContext.fcmToken.domain.FcmToken;
import com.fourtune.auction.boundedContext.notification.port.out.NotificationSettingsRepository;
import com.fourtune.auction.boundedContext.fcmToken.port.out.FcmTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FcmService {

    private final NotificationSettingsRepository notificationSettingsRepository;
    private final FcmSender fcmSender;
    private final FcmTokenRepository fcmTokenRepository;

    @Transactional(readOnly = true)
    public void sendNotification(Long userId, String type, String title, String message) {
        NotificationSettings settings = notificationSettingsRepository.findByUserId(userId)
                .orElse(new NotificationSettings(null));

        if (!isAllowed(settings, type)) {
            log.warn("알림 차단됨 (User ID: {}, Type: {})", userId, type);
            return;
        }
        List<String> tokens = fcmTokenRepository.findAllTokensByUserId(userId);
        List<String> uniqueTokens = tokens.stream().distinct().toList();
        if (tokens.isEmpty()) {
            log.warn("🚫 전송 실패: 해당 유저의 FCM 토큰이 없음 (User ID: {})", userId);
            return;
        }
        fcmSender.sendMulticast(uniqueTokens, title, message);
    }

    @Transactional
    public void saveToken(Long userId, String token) {
        fcmTokenRepository.findByToken(token)
                .ifPresentOrElse(
                        FcmToken::updateLastUsedAt,
                        () -> fcmTokenRepository.save(new FcmToken(userId, token))
                );

        log.info("FCM Token saved for userId: {}", userId);
    }

    private boolean isAllowed(NotificationSettings setting, String type) {
        return switch (type) {
            case "OUTBID", "AUCTION_SUCCESS", "AUCTION_FAILED", "BID_RECEIVED" -> setting.isBidPushEnabled();
            case "PAYMENT" -> setting.isPaymentPushEnabled();
            case "WATCHLIST_START", "WATCHLIST_END" -> setting.isWatchListPushEnabled();
            default -> true;
        };
    }

}
