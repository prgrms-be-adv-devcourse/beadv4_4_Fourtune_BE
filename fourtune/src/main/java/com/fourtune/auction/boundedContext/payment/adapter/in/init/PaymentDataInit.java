package com.fourtune.auction.boundedContext.payment.adapter.in.init;

import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
import com.fourtune.auction.boundedContext.payment.domain.constant.CashPolicy;
import com.fourtune.auction.boundedContext.payment.domain.entity.CashLog;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
public class PaymentDataInit {

    private final PaymentDataInit self;
    private final PaymentFacade paymentFacade;
    private final PaymentUserRepository paymentUserRepository;
    private final WalletRepository walletRepository;

    public PaymentDataInit(
            @Lazy PaymentDataInit self,
            PaymentFacade paymentFacade,
            PaymentUserRepository paymentUserRepository,
            WalletRepository walletRepository
    ) {
        this.self = self;
        this.paymentFacade = paymentFacade;
        this.paymentUserRepository = paymentUserRepository;
        this.walletRepository = walletRepository;
    }

    @Bean
    @Order(1)
    public ApplicationRunner cashDataInitApplicationRunner() {
        return args -> {
            self.makeSystemUser();
        };
    }

    @Transactional
    public void makeSystemUser() {
        //TODO: payment user 에만 저장함. 동기화 할때 충돌 예상 수정필요

        // 1. System User 확보 (없으면 생성, 있으면 조회)
        PaymentUser systemUser = paymentUserRepository.findByNickname("system")
                .orElseGet(() -> {
                    PaymentUser newUser = PaymentUser.builder()
                            .email("system@email.com")
                            .password("password")
                            .nickname("system")
                            .role("USER")
                            .id(CashPolicy.SYSTEM_MEMBER_ID)
                            .deletedAt(null)
                            .createdAt(LocalDateTime.now())
                            .phoneNumber("010-1234-1234")
                            .updatedAt(null)
                            .status("ACTIVE")
                            .build();
                    return paymentUserRepository.save(newUser);
                });

// 2. System Wallet 확보
// 위에서 얻은 systemUser 객체를 바로 사용하므로 .get() 호출 필요 없음
        Wallet systemWallet = paymentFacade.findSystemWallet()
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .paymentUser(systemUser)
                            .balance(0L)
                            .build();
                    return walletRepository.save(newWallet);
                });

        log.info("system user : {}, id = {}", systemUser.getNickname(), systemUser.getId());
        log.info("system wallet : {}", systemWallet.getId());

    }
}