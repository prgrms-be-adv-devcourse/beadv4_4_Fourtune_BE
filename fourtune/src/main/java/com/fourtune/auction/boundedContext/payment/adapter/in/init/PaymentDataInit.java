package com.fourtune.auction.boundedContext.payment.adapter.in.init;

import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
import com.fourtune.auction.boundedContext.payment.domain.constant.CashPolicy;
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
        Optional<PaymentUser> systemUser = paymentUserRepository.findByNickname("system");

        if(systemUser.isEmpty()) {
            PaymentUser paymentUser = PaymentUser.builder()
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
            paymentUserRepository.save(paymentUser);

            systemUser = paymentUserRepository.findByNickname("system");
        }

        Optional<Wallet> systemWwallet = paymentFacade.findSystemWallet();

        if(systemWwallet.isEmpty()){

            Wallet wallet = Wallet.builder()
                    .paymentUser(systemUser.get())
                    .balance(0L)
                    .cashLogs(null)
                    .build();
            walletRepository.save(wallet);
            systemWwallet = paymentFacade.findSystemWallet();
        }

        log.info("system user : "+systemUser.get().getNickname() + ", id = "+systemUser.get().getId());

        log.info("system wallet : "+systemWwallet.get().getId());

    }
}