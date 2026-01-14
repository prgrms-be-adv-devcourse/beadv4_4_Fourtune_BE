package com.fourtune.auction.boundedContext.user.application.service;

import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.global.error.ErrorCode;
import com.fourtune.auction.global.error.exception.BusinessException;
import com.fourtune.auction.shared.user.dto.UserWithdrawRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDeletedUseCase {

    private final UserSupport userSupport;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void userDelete(Long userId, UserWithdrawRequest request) {
        User user = userSupport.findByIdOrThrow(userId);

        if (user.getStatus() == Status.SUSPENDED) {
            throw new BusinessException(ErrorCode.ALREADY_WITHDRAWN);
        }

        validatePassword(request.password(), user.getPassword());
        validateCanWithdraw(user);

        user.withdraw();
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
