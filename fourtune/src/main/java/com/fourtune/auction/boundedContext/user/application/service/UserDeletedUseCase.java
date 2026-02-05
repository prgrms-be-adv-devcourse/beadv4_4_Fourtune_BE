package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.constant.UserEventType;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.config.EventPublishingConfig;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.global.outbox.service.OutboxService;
import com.fourtune.auction.shared.user.dto.UserWithdrawRequest;
import com.fourtune.auction.shared.user.event.UserDeletedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDeletedUseCase {

    private static final String AGGREGATE_TYPE = "User";

    private final UserSupport userSupport;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

    @Transactional
    public void userDelete(Long userId, UserWithdrawRequest request) {
        User user = userSupport.findByIdOrThrow(userId);

        if (user.getStatus() == Status.SUSPENDED) {
            throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN);
        }

        validatePassword(request.password(), user.getPassword());
        validateCanWithdraw(user);

        user.withdraw();

        // 이벤트 발송
        publishUserDeletedEvent(user);
    }

    private void publishUserDeletedEvent(User user) {
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            outboxService.append(AGGREGATE_TYPE, user.getId(), UserEventType.USER_DELETED.name(), user.toDto());
        } else {
            eventPublisher.publish(new UserDeletedEvent(user.toDto()));
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }
    }

    private void validateCanWithdraw(User user) {
        // 예시: 진행 중인 경매나 거래가 있다면 탈퇴 막기
        // if (auctionRepository.existsBySellerAndStatus(user, "ONGOING")) {
        //     throw new BusinessException(ErrorCode.CANNOT_WITHDRAW_ON_AUCTION);
        // }
    }

}
