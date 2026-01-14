package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserSupport {

    private final UserRepository userRepository;

    public long count(){
        return userRepository.count();
    }

    public Optional<User> findByUserNickname(String nickname){
        return userRepository.findByNickname(nickname);
    }

    User findByIdOrThrow(long id){
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    public User save(User user){
        return userRepository.save(user);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    public User findActiveUserByEmailOrThrow(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.LOGIN_INPUT_INVALID));

        if (user.getStatus() != Status.ACTIVE) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        return user;
    }

}



