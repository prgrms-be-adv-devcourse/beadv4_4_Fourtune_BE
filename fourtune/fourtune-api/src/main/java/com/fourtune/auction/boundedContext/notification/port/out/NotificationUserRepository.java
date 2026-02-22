package com.fourtune.auction.boundedContext.notification.port.out;

import com.fourtune.auction.boundedContext.notification.domain.NotificationUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationUserRepository extends JpaRepository<NotificationUser, Long> {
}
