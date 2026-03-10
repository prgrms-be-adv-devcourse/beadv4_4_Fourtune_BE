package com.fourtune.config;

import com.fourtune.payment.domain.entity.PaymentUser;
import com.fourtune.payment.domain.entity.Wallet;
import com.fourtune.payment.port.out.PaymentUserRepository;
import com.fourtune.payment.port.out.WalletRepository;
import com.fourtune.shared.payment.constant.CashPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 시스템 지갑 유저 초기화.
 *
 * payment-service가 기동될 때마다 실행되며,
 * 시스템 보관 지갑(holding@system.com)과 플랫폼 수익 지갑(revenue@platform.com)이
 * payment_db에 없으면 자동 생성합니다.
 *
 * 이 초기화는 멱등합니다 — 이미 존재하면 아무 작업도 하지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemWalletInitializer implements ApplicationRunner {

    private static final long SYSTEM_USER_ID   = -1L;
    private static final long PLATFORM_USER_ID = -2L;

    private final PaymentUserRepository paymentUserRepository;
    private final WalletRepository walletRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        ensureSystemUser(SYSTEM_USER_ID,   CashPolicy.SYSTEM_HOLDING_USER_EMAIL,   "SYSTEM_HOLDING");
        ensureSystemUser(PLATFORM_USER_ID, CashPolicy.PLATFORM_REVENUE_USER_EMAIL, "PLATFORM_REVENUE");
        log.info("[SystemWalletInitializer] 시스템 지갑 초기화 완료");
    }

    private void ensureSystemUser(Long id, String email, String nickname) {
        boolean userExists = paymentUserRepository.existsById(id);
        if (!userExists) {
            PaymentUser user = PaymentUser.builder()
                    .id(id)
                    .email(email)
                    .nickname(nickname)
                    .password("")
                    .phoneNumber("")
                    .status("활동중")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            paymentUserRepository.save(user);
            log.info("[SystemWalletInitializer] 시스템 유저 생성: email={}", email);
        }

        PaymentUser user = paymentUserRepository.findById(id).orElseThrow();
        boolean walletExists = walletRepository.findWalletByPaymentUser(user).isPresent();
        if (!walletExists) {
            Wallet wallet = Wallet.builder()
                    .paymentUser(user)
                    .balance(0L)
                    .cashLogs(List.of())
                    .build();
            walletRepository.save(wallet);
            log.info("[SystemWalletInitializer] 시스템 지갑 생성: email={}", email);
        }
    }
}
