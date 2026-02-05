package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.config.EventPublishingConfig;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import com.fourtune.auction.global.outbox.service.OutboxService;
import com.fourtune.auction.shared.user.dto.UserSignUpRequest;
import com.fourtune.auction.shared.user.event.UserJoinedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserJoinedUseCase {

    private final UserSupport userSupport;
    private final PasswordEncoder passwordEncoder;
    private final EventPublisher eventPublisher;
    private final EventPublishingConfig eventPublishingConfig;
    private final OutboxService outboxService;

    @Transactional
    public void userJoin(UserSignUpRequest request) {
        Optional<User> existingUser = userSupport.findByEmail(request.email());

        if(existingUser.isPresent()){
            User user = existingUser.get();
            isSuspendedUser(user, request);
            publishUserJoinedEvent(user);
        }
        else{
            validateSignUp(request);
            User newUser = request.toEntity(passwordEncoder.encode(request.password()));
            userSupport.save(newUser);
            publishUserJoinedEvent(newUser);
        }
    }

    private void publishUserJoinedEvent(User user) {
        if (eventPublishingConfig.isUserEventsKafkaEnabled()) {
            outboxService.saveUserJoinedEvent(user.toDto());
        } else {
            eventPublisher.publish(new UserJoinedEvent(user.toDto()));
        }
    }

    private void isSuspendedUser(User user, UserSignUpRequest request){
        existingActiveUser(user);

        validateRejoin(user, request);

        user.changeStatus(Status.ACTIVE);
        user.changePassword(passwordEncoder.encode(request.password()));
        user.updateNickname(request.nickname());
    }

    private void validateRejoin(User currentUser, UserSignUpRequest request) {
        if (!currentUser.getNickname().equals(request.nickname())
                && userSupport.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATION);
        }

        if (!currentUser.getPhoneNumber().equals(request.phoneNumber())
                && userSupport.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.PHONE_DUPLICATION);
        }
    }

    private void existingActiveUser(User user){
        if (user.getStatus() != Status.SUSPENDED) {
            throw new BusinessException(ErrorCode.EMAIL_DUPLICATION);
        }
    }

    private void validateSignUp(UserSignUpRequest request) {
        if (userSupport.existsByNickname(request.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_DUPLICATION);
        }

        if (userSupport.existsByPhoneNumber(request.phoneNumber())) {
            throw new BusinessException(ErrorCode.PHONE_DUPLICATION);
        }
    }
}
