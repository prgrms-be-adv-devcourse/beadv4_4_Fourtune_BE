package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.shared.user.dto.*;
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

}

