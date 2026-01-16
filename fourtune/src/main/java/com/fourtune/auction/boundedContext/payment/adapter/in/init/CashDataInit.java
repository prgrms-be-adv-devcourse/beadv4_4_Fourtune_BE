package com.fourtune.auction.boundedContext.payment.adapter.in.init;

import com.fourtune.auction.boundedContext.payment.application.service.CashFacade;
import com.fourtune.auction.boundedContext.payment.domain.constant.CashPolicy;
import com.fourtune.auction.boundedContext.payment.domain.entity.CashUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.CashUserRepository;
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
public class CashDataInit {

    private final CashDataInit self;
    private final CashFacade cashFacade;
    private final CashUserRepository cashUserRepository;
    private final WalletRepository walletRepository;

    public CashDataInit(
            @Lazy CashDataInit self,
            CashFacade cashFacade,
            CashUserRepository cashUserRepository,
            WalletRepository walletRepository
    ) {
        this.self = self;
        this.cashFacade = cashFacade;
        this.cashUserRepository = cashUserRepository;
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
        Optional<CashUser> systemUser = cashUserRepository.findByNickname("system");

        if(systemUser.isEmpty()) {
            CashUser cashUser = CashUser.builder()
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
            cashUserRepository.save(cashUser);

            systemUser = cashUserRepository.findByNickname("system");
        }

        Optional<Wallet> systemWwallet = cashFacade.findSystemWallet();

        if(systemWwallet.isEmpty()){

            Wallet wallet = Wallet.builder()
                    .user(systemUser.get())
                    .balance(0)
                    .cashLogs(null)
                    .build();
            walletRepository.save(wallet);
            systemWwallet = cashFacade.findSystemWallet();
        }

        log.info("system user : "+systemUser.get().getNickname() + ", id = "+systemUser.get().getId());

        log.info("system wallet : "+systemWwallet.get().getId());

    }
}