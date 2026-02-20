package com.fourtune.auction.boundedContext.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.auction.boundedContext.notification.domain.NotificationUser;
import com.fourtune.core.config.EventPublishingConfig;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.notification.event.NotificationEvent;
import com.fourtune.kafka.notification.NotificationEventType;
import com.fourtune.kafka.notification.NotificationKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationCreateUseCase {

    private final NotificationSupport notificationSupport;
    private final EventPublisher eventPublisher;
    private final EventPublishingConfig eventPublishingConfig;
    private final ObjectMapper objectMapper;
    private final NotificationKafkaProducer notificationKafkaProducer;

    @Transactional
    public void bidPlaceToSeller(Long sellerId, Long bidderId, Long auctionId, NotificationType type){
        if (sellerId.equals(bidderId)) {
            throw new BusinessException(ErrorCode.SELF_BIDDING_NOT_ALLOWED);
        }
        
        String relatedUrl = "/auctions/" + auctionId;
        createNotification(sellerId, relatedUrl, type);
    }

    @Transactional
    public void createNotificationWithUrl(Long receiverId, Long auctionId, NotificationType type){
        String relatedUrl = "/auctions/" + auctionId;

        createNotification(receiverId, relatedUrl, type);
    }

    @Transactional
    public void createGroupNotification(List<Long> userIds, Long auctionId, NotificationType type) {
        String relatedUrl = "/auctions/" + auctionId;

        for (Long userId : userIds) {
            createNotification(userId, relatedUrl, type);
        }
    }

    @Transactional
    public void createSettlementNotification(Long receiverId, Long settlementId, NotificationType type) {
        String relatedUrl = "/settlements/" + settlementId;
        createNotification(receiverId, relatedUrl, type);
    }

    private void createNotification(Long receiverId, String relatedUrl, NotificationType type){
        NotificationUser user = notificationSupport.findByUserId(receiverId);

        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(type.getTitleTemplate())
                .content(type.getContentTemplate())
                .relatedUrl(relatedUrl)
                .build();

        notificationSupport.save(notification);
        log.info("알림 생성 완료 - Receiver: {}, Type: {}", receiverId, type);

        if (eventPublishingConfig.isNotificationEventsKafkaEnabled()) {
            try {
                String payload = objectMapper.writeValueAsString(Map.of(
                        "receiverId", receiverId,
                        "title", notification.getTitle(),
                        "content", notification.getContent(),
                        "relatedUrl", relatedUrl));
                notificationKafkaProducer.send(
                        String.valueOf(receiverId), payload, NotificationEventType.NOTIFICATION_CREATED.name());
            } catch (Exception e) {
                log.error("Notification Kafka 이벤트 발행 실패: receiverId={}", receiverId, e);
            }
        } else {
            eventPublisher.publish(new NotificationEvent(receiverId, notification.getTitle(), notification.getContent(), relatedUrl));
        }
    }

}
