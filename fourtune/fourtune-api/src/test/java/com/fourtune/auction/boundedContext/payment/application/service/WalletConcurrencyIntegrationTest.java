package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import com.fourtune.core.error.ErrorCode;
import com.fourtune.core.error.exception.BusinessException;
import com.fourtune.shared.payment.constant.CashPolicy;
import com.fourtune.shared.settlement.dto.SettlementDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * WAL-02: 지갑 동시 출금 시 레이스 컨디션 검증
 * - 잔액 1,000원에서 600원 차감 2건 동시 요청 시, 1건만 성공·1건 잔액 부족이어야 함 (락 미적용 시 현재는 실패할 수 있음
 * → TDD)
 * - 설정 데이터가 워커 스레드에서 보이도록 본 테스트 메서드만 @Transactional 제거 (스레드별 트랜잭션에서 커밋된 데이터 조회)
 */
@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.elasticsearch.repositories.enabled=false",
        "feature.kafka.enabled=false"
})
class WalletConcurrencyIntegrationTest {

    @MockitoBean
    private com.google.firebase.messaging.FirebaseMessaging firebaseMessaging;
    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.in.event.AuctionItemIndexEventListener auctionItemIndexEventListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.search.adapter.out.elasticsearch.ElasticsearchAuctionItemIndexingHandler elasticsearchAuctionItemIndexingHandler;
    @MockitoBean
    private com.fourtune.auction.boundedContext.payment.adapter.in.kafka.PaymentKafkaListener paymentKafkaListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.payment.adapter.in.kafka.PaymentUserKafkaListener paymentUserKafkaListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.payment.adapter.in.kafka.PaymentSettlementKafkaListener paymentSettlementKafkaListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.watchList.adapter.in.kafka.WatchListUserKafkaListener watchListUserKafkaListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.settlement.adapter.in.kafka.SettlementUserKafkaListener settlementUserKafkaListener;
    @MockitoBean
    private com.fourtune.auction.boundedContext.notification.adapter.in.kafka.NotificationUserKafkaListener notificationUserKafkaListener;

    @Autowired
    private PaymentCompleteSettlementUseCase paymentCompleteSettlementUseCase;
    @Autowired
    private PaymentUserRepository paymentUserRepository;
    @Autowired
    private WalletRepository walletRepository;

    private PaymentUser systemUser;
    private PaymentUser platformUser;
    private Wallet systemWallet;
    private static final long INITIAL_BALANCE = 1_000L;
    private static final long DEBIT_AMOUNT = 600L;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        systemUser = paymentUserRepository.findByEmail(CashPolicy.SYSTEM_HOLDING_USER_EMAIL)
                .orElseGet(() -> paymentUserRepository.save(
                        PaymentUser.builder()
                                .id(1L)
                                .email(CashPolicy.SYSTEM_HOLDING_USER_EMAIL)
                                .nickname("system-holding")
                                .password("")
                                .phoneNumber("")
                                .createdAt(now)
                                .updatedAt(now)
                                .deletedAt(null)
                                .status("ACTIVE")
                                .build()));
        platformUser = paymentUserRepository.findByEmail(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                .orElseGet(() -> paymentUserRepository.save(
                        PaymentUser.builder()
                                .id(2L)
                                .email(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                                .nickname("platform-revenue")
                                .password("")
                                .phoneNumber("")
                                .createdAt(now)
                                .updatedAt(now)
                                .deletedAt(null)
                                .status("ACTIVE")
                                .build()));

        // 동시성 테스트용: 시스템 지갑을 잔액 1,000원인 새 지갑으로 준비 (credit() 미사용으로
        // LazyInitializationException 회피)
        walletRepository.findWalletByPaymentUser(systemUser).ifPresent(walletRepository::delete);
        systemWallet = walletRepository.save(Wallet.builder().paymentUser(systemUser).balance(INITIAL_BALANCE).build());

        walletRepository.findWalletByPaymentUser(platformUser)
                .orElseGet(() -> walletRepository.save(Wallet.builder().paymentUser(platformUser).balance(0L).build()));
    }

    @Test
    @DisplayName("[WAL-02] 잔액 1,000원에서 600원 차감 2건 동시 요청 시, 1건만 성공·최종 잔액 400원이어야 함 (동시성)")
    void settlementCashComplete_concurrentTwoDebits_onlyOneSucceedsBalance400() throws InterruptedException {
        int threadCount = 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        List<Throwable> failures = new ArrayList<>();

        SettlementDto dto1 = SettlementDto.builder()
                .id(1L)
                .payeeId(platformUser.getId())
                .payeeEmail(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                .amount(DEBIT_AMOUNT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .settledAt(LocalDateTime.now())
                .build();
        SettlementDto dto2 = SettlementDto.builder()
                .id(2L)
                .payeeId(platformUser.getId())
                .payeeEmail(CashPolicy.PLATFORM_REVENUE_USER_EMAIL)
                .amount(DEBIT_AMOUNT)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .settledAt(LocalDateTime.now())
                .build();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final SettlementDto dto = (i == 0) ? dto1 : dto2;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    paymentCompleteSettlementUseCase.settlementCashComplete(dto);
                    successCount.incrementAndGet();
                } catch (BusinessException e) {
                    if (ErrorCode.PAYMENT_WALLET_INSUFFICIENT_BALANCE.equals(e.getErrorCode())) {
                        // 기대: 한 쪽은 잔액 부족
                    } else {
                        failures.add(e);
                    }
                } catch (Throwable t) {
                    failures.add(t);
                } finally {
                    endLatch.countDown();
                }
            });
        }
        startLatch.countDown();
        endLatch.await();
        executor.shutdown();

        assertThat(failures).isEmpty();

        Wallet system = walletRepository.findWalletByPaymentUser(systemUser).orElseThrow();
        long finalBalance = system.getBalance();

        assertThat(successCount.get())
                .as("동시 2건 중 정확히 1건만 성공해야 함 (락 적용 시). 현재 락 미적용이면 2건 성공·잔액 -200 가능")
                .isEqualTo(1);
        assertThat(finalBalance)
                .as("최종 시스템 지갑 잔액은 1000 - 600 = 400이어야 함")
                .isEqualTo(INITIAL_BALANCE - DEBIT_AMOUNT);
    }
}
