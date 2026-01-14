package com.fourtune.auction.boundedContext.cash.in;

import com.fourtune.auction.boundedContext.cash.app.CashFacade;
import com.fourtune.auction.boundedContext.cash.domain.CashPolicy;
import com.fourtune.auction.boundedContext.cash.domain.CashUser;
import com.fourtune.auction.boundedContext.cash.out.CashUserRepository;
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

    public CashDataInit(
            @Lazy CashDataInit self,
            CashFacade cashFacade,
            CashUserRepository cashUserRepository
    ) {
        this.self = self;
        this.cashFacade = cashFacade;
        this.cashUserRepository = cashUserRepository;
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

        log.info("system user : "+systemUser.get().getNickname() + ", id = "+systemUser.get().getId());


    }
}