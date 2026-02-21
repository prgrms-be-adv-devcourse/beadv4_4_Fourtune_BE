package com.fourtune.outbox.repository;

import com.fourtune.outbox.domain.OutboxEvent;
import com.fourtune.outbox.domain.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * 발행 대기 중인 이벤트 조회 (생성 시간순)
     */
    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    /**
     * 발행 대기 중인 이벤트 조회 (제한)
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status ORDER BY o.createdAt ASC LIMIT :limit")
    List<OutboxEvent> findPendingEvents(@Param("status") OutboxEventStatus status, @Param("limit") int limit);

    /**
     * 재시도 가능한 실패 이벤트 조회
     */
    @Query("SELECT o FROM OutboxEvent o WHERE o.status = :status AND o.retryCount < :maxRetry ORDER BY o.createdAt ASC")
    List<OutboxEvent> findRetryableEvents(@Param("status") OutboxEventStatus status, @Param("maxRetry") int maxRetry);

    /**
     * 오래된 발행 완료 이벤트 삭제 (정리용)
     */
    @Modifying
    @Query("DELETE FROM OutboxEvent o WHERE o.status = :status AND o.publishedAt < :before")
    int deleteOldPublishedEvents(@Param("status") OutboxEventStatus status, @Param("before") LocalDateTime before);
}
