package com.fourtune.auction.boundedContext.notification.application.service;

import com.fourtune.auction.boundedContext.notification.constant.NotificationType;
import com.fourtune.auction.boundedContext.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 알림 발송 UseCase
 * - 알림 생성 및 발송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSendUseCase {

    private final NotificationSupport notificationSupport;

    /**
     * 알림 발송
     */
    @Transactional
    public Notification send(Long userId, NotificationType type, String title, String content) {
        Notification notification = Notification.create(userId, type, title, content);
        Notification saved = notificationSupport.save(notification);
        
        log.info("알림 발송 완료: userId={}, type={}, title={}", userId, type, title);
        
        return saved;
    }

    /**
     * 관련 ID 포함 알림 발송
     */
    @Transactional
    public Notification sendWithRelatedId(Long userId, NotificationType type, 
                                          String title, String content, Long relatedId) {
        Notification notification = Notification.createWithRelatedId(
                userId, type, title, content, relatedId);
        Notification saved = notificationSupport.save(notification);
        
        log.info("알림 발송 완료: userId={}, type={}, relatedId={}", userId, type, relatedId);
        
        return saved;
    }

    /**
     * 입찰 알림 발송 (판매자에게)
     */
    @Transactional
    public Notification sendBidNotification(Long sellerId, Long auctionId, String auctionTitle, String bidderName) {
        String title = "새로운 입찰이 등록되었습니다";
        String content = String.format("'%s' 상품에 %s님이 입찰하였습니다.", auctionTitle, bidderName);
        
        return sendWithRelatedId(sellerId, NotificationType.BID_PLACED, title, content, auctionId);
    }

    /**
     * 상위 입찰 알림 발송 (이전 최고 입찰자에게)
     */
    @Transactional
    public Notification sendOutbidNotification(Long previousBidderId, Long auctionId, String auctionTitle) {
        String title = "더 높은 입찰이 등록되었습니다";
        String content = String.format("'%s' 상품에 더 높은 입찰가가 등록되었습니다. 확인해보세요!", auctionTitle);
        
        return sendWithRelatedId(previousBidderId, NotificationType.BID_OUTBID, title, content, auctionId);
    }

    /**
     * 낙찰 알림 발송
     */
    @Transactional
    public Notification sendWinNotification(Long winnerId, Long auctionId, String auctionTitle) {
        String title = "축하합니다! 낙찰되었습니다";
        String content = String.format("'%s' 상품에 낙찰되었습니다. 결제를 진행해주세요.", auctionTitle);
        
        return sendWithRelatedId(winnerId, NotificationType.AUCTION_WON, title, content, auctionId);
    }

    /**
     * 경매 종료 알림 발송 (판매자에게)
     */
    @Transactional
    public Notification sendAuctionEndedNotification(Long sellerId, Long auctionId, String auctionTitle, boolean hasBid) {
        String title = "경매가 종료되었습니다";
        String content = hasBid 
                ? String.format("'%s' 경매가 종료되어 낙찰자가 결정되었습니다.", auctionTitle)
                : String.format("'%s' 경매가 입찰 없이 종료되었습니다.", auctionTitle);
        
        return sendWithRelatedId(sellerId, NotificationType.AUCTION_ENDED, title, content, auctionId);
    }

    /**
     * 결제 완료 알림 발송
     */
    @Transactional
    public Notification sendPaymentCompletedNotification(Long userId, Long orderId, String orderName) {
        String title = "결제가 완료되었습니다";
        String content = String.format("'%s' 주문의 결제가 완료되었습니다.", orderName);
        
        return sendWithRelatedId(userId, NotificationType.PAYMENT_COMPLETED, title, content, orderId);
    }

}
