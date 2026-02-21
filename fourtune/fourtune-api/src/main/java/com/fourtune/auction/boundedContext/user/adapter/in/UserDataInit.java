package com.fourtune.auction.boundedContext.user.adapter.in;

import com.fourtune.auction.boundedContext.user.application.service.UserFacade;
import com.fourtune.shared.payment.constant.CashPolicy;
import com.fourtune.shared.user.dto.UserSignUpRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@Slf4j
public class UserDataInit {
    private final UserDataInit self;
    private final UserFacade userFacade;

    public UserDataInit(
            @Lazy UserDataInit self,
            UserFacade userFacade
    ) {
        this.self = self;
        this.userFacade = userFacade;
    }

    @Bean
    @Order(1)
    public ApplicationRunner memberDataInitApplicationRunner() {
        return args -> {
            self.makeBaseMembers();
            log.warn("초기 데이터 생성 성공");
        };
    }

    @Transactional
    public void makeBaseMembers() {
        if (userFacade.count() > 1) return;

        userFacade.signup(new UserSignUpRequest(CashPolicy.SYSTEM_HOLDING_USER_EMAIL, "1234", "system", "010-1111-1111"));
        userFacade.signup(new UserSignUpRequest(CashPolicy.PLATFORM_REVENUE_USER_EMAIL, "1234", "platform", "010-2222-2222"));
        userFacade.signup(new UserSignUpRequest("user1@google.com", "1234", "user1", "010-3333-3333"));
    }
}
