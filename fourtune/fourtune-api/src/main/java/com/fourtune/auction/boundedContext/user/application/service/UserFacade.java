package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.mapper.UserMapper;
import com.fourtune.common.shared.user.dto.*;
import com.fourtune.common.shared.user.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserFacade {

    private final UserJoinedUseCase userJoinedUseCase;
    private final UserLoginUseCase userLoginUseCase;
    private final UserModifiedUseCase userModifiedUseCase;
    private final UserPasswordChangeUseCase userPasswordChangeUseCase;
    private final UserDeletedUseCase userDeletedUseCase;
    private final UserSupport userSupport;

    public void signup(UserSignUpRequest request) {
        // 지금은 가입 UseCase 하나만 호출하지만,
        // 나중에 emailUseCase.sendWelcomeEmail(request.email()) 같은 게 추가되면
        // 여기서 조립하면 됩니다.
        userJoinedUseCase.userJoin(request);
    }

    public UserLoginResponse login(UserLoginRequest request) {
        return userLoginUseCase.userLogin(request);
    }

    public void updateProfile(Long userId, UserUpdateRequest request) {
        userModifiedUseCase.update(userId, request);
    }

    public void changePassword(Long userId, UserPasswordChangeRequest request) {
        userPasswordChangeUseCase.userChangePassword(userId, request);
    }

    public void withdraw(Long userId, UserWithdrawRequest request) {
        userDeletedUseCase.userDelete(userId, request);
    }

    public Long count(){
        return userSupport.count();
    }

    /**
     * ID로 유저 정보 조회 (id, email, nickname, status 등)
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return UserMapper.toDto(userSupport.findByIdOrThrow(id));
    }

    /**
     * ID 목록으로 닉네임 맵 조회 (경매/입찰/주문 DTO용, N+1 방지)
     */
    @Transactional(readOnly = true)
    public java.util.Map<Long, String> getNicknamesByIds(java.util.Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return java.util.Map.of();
        }
        return userSupport.findByIdIn(ids).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, User::getNickname, (a, b) -> a));
    }

}

