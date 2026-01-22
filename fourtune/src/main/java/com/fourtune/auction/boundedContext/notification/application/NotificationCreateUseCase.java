package com.fourtune.auction.boundedContext.notification.application;

import com.fourtune.auction.boundedContext.notification.domain.constant.NotificationType;
import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.auction.boundedContext.notification.domain.NotificationUser;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.shared.notification.dto.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationCreateUseCase {

    private final NotificationSupport notificationSupport;
    private final EventPublisher eventPublisher;

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

        eventPublisher.publish(new NotificationEvent(receiverId, notification.getTitle(), notification.getContent(), relatedUrl));
    }

}
