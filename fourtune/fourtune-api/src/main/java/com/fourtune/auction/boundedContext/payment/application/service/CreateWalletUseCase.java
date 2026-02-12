package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import com.fourtune.common.global.error.ErrorCode;
import com.fourtune.common.global.error.exception.BusinessException;
import com.fourtune.common.shared.payment.dto.PaymentUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateWalletUseCase {

    private final WalletRepository walletRepository;
    private final PaymentUserRepository paymentUserRepository;

    @Transactional
    public Wallet createWallet(PaymentUserDto dto) {

        PaymentUser paymentUser = paymentUserRepository.findByEmail(dto.getEmail()).orElseThrow(
                () -> new BusinessException(ErrorCode.PAYMENT_USER_NOT_FOUND)
        );

        Wallet newWallet = Wallet.builder()
                .paymentUser(paymentUser)
                .balance(0L)
                .build();

        return walletRepository.save(newWallet);
    }
}
