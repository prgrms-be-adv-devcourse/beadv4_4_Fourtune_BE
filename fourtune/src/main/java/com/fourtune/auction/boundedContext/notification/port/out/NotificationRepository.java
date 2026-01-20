package com.fourtune.auction.boundedContext.notification.port.out;

import com.fourtune.auction.boundedContext.notification.constant.NotificationType;
import com.fourtune.auction.boundedContext.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 알림 Repository
 */
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 사용자의 알림 목록 조회 (페이징)
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자의 읽지 않은 알림 목록 조회
     */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /**
     * 사용자의 읽지 않은 알림 개수 조회
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 사용자의 특정 타입 알림 조회
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type);

    /**
     * 사용자의 모든 알림 읽음 처리 (벌크 업데이트)
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = CURRENT_TIMESTAMP WHERE n.userId = :userId AND n.isRead = false")
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 사용자의 알림 전체 삭제
     */
    void deleteByUserId(Long userId);

    /**
     * 읽은 알림 삭제 (특정 사용자)
     */
    void deleteByUserIdAndIsReadTrue(Long userId);

}
