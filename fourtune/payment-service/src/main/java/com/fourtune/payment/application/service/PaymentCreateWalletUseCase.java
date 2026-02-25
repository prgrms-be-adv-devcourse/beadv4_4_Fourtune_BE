package com.fourtune.payment.application.service;

import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.payment.domain.entity.PaymentUser;
import com.fourtune.payment.domain.entity.Wallet;
import com.fourtune.payment.port.out.PaymentUserRepository;
import com.fourtune.payment.port.out.WalletRepository;
import com.fourtune.shared.payment.dto.PaymentUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentCreateWalletUseCase {

    private final WalletRepository walletRepository;
    private final PaymentUserRepository paymentUserRepository;

    @Transactional
    public Wallet createWallet(PaymentUserDto dto) {
        PaymentUser paymentUser = paymentUserRepository.findByEmail(dto.getEmail()).orElseThrow(
                () -> new BusinessException(ErrorCode.PAYMENT_USER_NOT_FOUND)
        );

        // 이미 지갑이 있으면 생성하지 않음 (유저 생성 이벤트 중복/재처리 시 멱등)
        return walletRepository.findWalletByPaymentUser(paymentUser)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder()
                                .paymentUser(paymentUser)
                                .balance(0L)
                                .build()));
    }
}
