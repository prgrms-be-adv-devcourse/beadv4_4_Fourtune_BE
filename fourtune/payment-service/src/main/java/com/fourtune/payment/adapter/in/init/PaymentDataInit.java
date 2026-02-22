package com.fourtune.payment.adapter.in.init;
//
//import com.fourtune.auction.boundedContext.payment.application.service.PaymentFacade;
//import com.fourtune.auction.shared.payment.constant.CashPolicy;
//import com.fourtune.auction.boundedContext.payment.domain.entity.CashLog;
//import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
//import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
//import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
//import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.core.annotation.Order;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//
//@Slf4j
//@Configuration
public class PaymentDataInit {
//
//    private final PaymentDataInit self;
//    private final PaymentFacade paymentFacade;
//    private final PaymentUserRepository paymentUserRepository;
//    private final WalletRepository walletRepository;
//
//    public PaymentDataInit(
//            @Lazy PaymentDataInit self,
//            PaymentFacade paymentFacade,
//            PaymentUserRepository paymentUserRepository,
//            WalletRepository walletRepository
//    ) {
//        this.self = self;
//        this.paymentFacade = paymentFacade;
//        this.paymentUserRepository = paymentUserRepository;
//        this.walletRepository = walletRepository;
//    }
//
//    @Bean
//    @Order(1)
//    public ApplicationRunner cashDataInitApplicationRunner() {
//        return args -> {
//            self.makeBasicUsers();
//        };
//    }
//
//    public void makeUser(String email){
//        // 1. System User 확보 (없으면 생성, 있으면 조회)
//        PaymentUser user = paymentUserRepository.findByEmail(email)
//                .orElseGet(() -> {
//                    PaymentUser newUser = PaymentUser.builder()
//                            .password("password")
//                            .email(CashPolicy.SYSTEM_HOLDING_USER_EMAIL)
//                            .nickname("system")
//                            .role("USER")
//                            .deletedAt(null)
//                            .createdAt(LocalDateTime.now())
//                            .phoneNumber("010-1234-1234")
//                            .updatedAt(null)
//                            .status("ACTIVE")
//                            .build();
//                    return paymentUserRepository.save(newUser);
//                });
//
//        // 2. System Wallet 확보
//        // 위에서 얻은 user 객체를 바로 사용하므로 .get() 호출 필요 없음
//        Wallet wallet = paymentFacade.findWalletByUserEmail(email)
//                .orElseGet(() -> {
//                    Wallet newWallet = Wallet.builder()
//                            .paymentUser(user)
//                            .balance(0L)
//                            .build();
//                    return walletRepository.save(newWallet);
//                });
//
//        log.info("user : {}, id = {}", user.getNickname(), user.getId());
//        log.info("wallet : {}의 지갑", user.getId());
//    }
//
//    @Transactional
//    public void makeBasicUsers() {
//        //TODO: payment user 에만 저장함. 동기화 할때 충돌 예상 수정필요
//        makeUser(CashPolicy.SYSTEM_HOLDING_USER_EMAIL);
//        makeUser(CashPolicy.PLATFORM_REVENUE_USER_EMAIL);
//    }
}
