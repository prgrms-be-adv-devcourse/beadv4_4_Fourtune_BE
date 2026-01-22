package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
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

    @Transactional
    public void userJoin(UserSignUpRequest request) {
        Optional<User> existingUser = userSupport.findByEmail(request.email());

        if(existingUser.isPresent()){
            User user = existingUser.get();
            isSuspendedUser(user, request);
            eventPublisher.publish(new UserJoinedEvent(user.toDto()));
        }
        else{
            validateSignUp(request);
            User newUser = request.toEntity(passwordEncoder.encode(request.password()));
            userSupport.save(newUser);
            eventPublisher.publish(new UserJoinedEvent(newUser.toDto()));
        }
    }

    private void isSuspendedUser(User user, UserSignUpRequest request){
            existingActiveUser(user);
            validateSignUp(request);

            user.changeStatus(Status.ACTIVE);
            user.changePassword(passwordEncoder.encode(request.password()));
            user.updateNickname(request.nickname());
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
