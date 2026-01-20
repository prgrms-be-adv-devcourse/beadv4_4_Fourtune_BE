package com.fourtune.auction.boundedContext.notification.application.service;

import com.fourtune.auction.boundedContext.notification.constant.NotificationType;
import com.fourtune.auction.boundedContext.notification.domain.Notification;
import com.fourtune.auction.shared.notification.dto.NotificationResponse;
import com.fourtune.auction.shared.notification.dto.UnreadCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 알림 Facade
 * - 여러 UseCase 조합 및 복잡한 플로우 조율
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationFacade {

    private final NotificationSendUseCase notificationSendUseCase;
    private final NotificationReadUseCase notificationReadUseCase;
    private final NotificationQueryUseCase notificationQueryUseCase;
    private final NotificationDeleteUseCase notificationDeleteUseCase;

    // ==================== 조회 ====================

    /**
     * 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return notificationQueryUseCase.getNotifications(userId, pageable);
    }

    /**
     * 읽지 않은 알림 목록 조회
     */
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return notificationQueryUseCase.getUnreadNotifications(userId);
    }

    /**
     * 읽지 않은 알림 개수 조회
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        return notificationQueryUseCase.getUnreadCount(userId);
    }

    /**
     * 알림 상세 조회
     */
    @Transactional(readOnly = true)
    public NotificationResponse getNotification(Long notificationId, Long userId) {
        return notificationQueryUseCase.getNotification(notificationId, userId);
    }

    // ==================== 읽음 처리 ====================

    /**
     * 단일 알림 읽음 처리
     */
    public void markAsRead(Long notificationId, Long userId) {
        notificationReadUseCase.markAsRead(notificationId, userId);
    }

    /**
     * 전체 알림 읽음 처리
     */
    public int markAllAsRead(Long userId) {
        return notificationReadUseCase.markAllAsRead(userId);
    }

    // ==================== 삭제 ====================

    /**
     * 단일 알림 삭제
     */
    public void deleteNotification(Long notificationId, Long userId) {
        notificationDeleteUseCase.delete(notificationId, userId);
    }

    /**
     * 읽은 알림 전체 삭제
     */
    public void deleteReadNotifications(Long userId) {
        notificationDeleteUseCase.deleteReadNotifications(userId);
    }

    // ==================== 발송 (이벤트 리스너에서 호출) ====================

    /**
     * 입찰 알림 발송
     */
    public Notification sendBidNotification(Long sellerId, Long auctionId, String auctionTitle, String bidderName) {
        return notificationSendUseCase.sendBidNotification(sellerId, auctionId, auctionTitle, bidderName);
    }

    /**
     * 상위 입찰 알림 발송
     */
    public Notification sendOutbidNotification(Long previousBidderId, Long auctionId, String auctionTitle) {
        return notificationSendUseCase.sendOutbidNotification(previousBidderId, auctionId, auctionTitle);
    }

    /**
     * 낙찰 알림 발송
     */
    public Notification sendWinNotification(Long winnerId, Long auctionId, String auctionTitle) {
        return notificationSendUseCase.sendWinNotification(winnerId, auctionId, auctionTitle);
    }

    /**
     * 경매 종료 알림 발송
     */
    public Notification sendAuctionEndedNotification(Long sellerId, Long auctionId, String auctionTitle, boolean hasBid) {
        return notificationSendUseCase.sendAuctionEndedNotification(sellerId, auctionId, auctionTitle, hasBid);
    }

    /**
     * 결제 완료 알림 발송
     */
    public Notification sendPaymentCompletedNotification(Long userId, Long orderId, String orderName) {
        return notificationSendUseCase.sendPaymentCompletedNotification(userId, orderId, orderName);
    }

    /**
     * 일반 알림 발송
     */
    public Notification sendNotification(Long userId, NotificationType type, String title, String content) {
        return notificationSendUseCase.send(userId, type, title, content);
    }

}
