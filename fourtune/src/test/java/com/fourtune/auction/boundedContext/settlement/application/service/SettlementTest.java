package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.payment.application.service.PaymentSupport;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementPolicy;
import com.fourtune.auction.boundedContext.settlement.domain.entity.Settlement;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementCandidatedItemRepository;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementRepository;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementUserRepository;
import com.fourtune.auction.shared.payment.constant.CashPolicy;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SettlementIntegrationTest {

    @Autowired CollectSettlementItemChunkUseCase collectUseCase;
    @Autowired CompleteSettlementChunkUseCase completeUseCase;

    @Autowired SettlementRepository settlementRepository;
    @Autowired SettlementCandidatedItemRepository settlementCandidatedItemRepository;
    @Autowired SettlementUserRepository settlementUserRepository;

    @Autowired PaymentUserRepository paymentUserRepository;
    @Autowired WalletRepository walletRepository;
    @Autowired PaymentSupport paymentSupport;
    @Autowired EntityManager em;

    SettlementUser seller;
    SettlementUser buyer;
    SettlementUser platform;

    @BeforeEach
    void setUp() {
        settlementCandidatedItemRepository.deleteAll();

        // 1. 정산 유저 생성
        seller = createSettlementUserIfAbsent(10L, "Seller", "seller@test.com");
        buyer = createSettlementUserIfAbsent(20L, "Buyer", "buyer@test.com");
        platform = createSettlementUserIfAbsent(9999L, "Platform", CashPolicy.PLATFORM_REVENUE_USER_EMAIL);

        // 2. 결제 유저 및 지갑 생성
        createPaymentUserAndWalletIfAbsent(seller.getId(), seller.getEmail());
        createPaymentUserAndWalletIfAbsent(8888L, CashPolicy.SYSTEM_HOLDING_USER_EMAIL);
        createPaymentUserAndWalletIfAbsent(8889L, CashPolicy.PLATFORM_REVENUE_USER_EMAIL);
    }

    @Test
    @DisplayName("[수집] 구매확정 기간이 지난 후보 아이템들을 모아서 정산서(Settlement)에 연결한다")
    void collectSettlementItemChunk_Success() {
        // ... (동일)
        Settlement activeSettlement = createActiveSettlement(seller);
        long waitingDays = SettlementPolicy.SETTLEMENT_WAITING_DAYS.getValue();
        LocalDateTime pastDate = LocalDateTime.now().minusDays(waitingDays + 1);
        SettlementCandidatedItem targetItem = createCandidateItem(seller, buyer, 10000L, pastDate, "ITEM_001");
        SettlementCandidatedItem futureItem = createCandidateItem(seller, buyer, 5000L, LocalDateTime.now().minusDays(0), "ITEM_002");

        int processedCount = collectUseCase.collectSettlementItemChunk(10);

        assertThat(processedCount).isEqualTo(1);
        SettlementCandidatedItem updatedTarget = settlementCandidatedItemRepository.findById(targetItem.getId()).orElseThrow();
        assertThat(updatedTarget.getSettlementItem()).isNotNull();
        assertThat(updatedTarget.getSettlementItem().getAmount()).isEqualTo(10000L);
        SettlementCandidatedItem updatedFuture = settlementCandidatedItemRepository.findById(futureItem.getId()).orElseThrow();
        assertThat(updatedFuture.getSettlementItem()).isNull();
    }

    @Test
    @DisplayName("[완료] 정산되지 않은 정산서를 확정(Complete) 처리하고 정산일시를 기록한다")
    void completeSettlementChunk_Success() {
        // Given
        Long settlementAmount = 50000L;

        Settlement activeSettlement = Settlement.builder()
                .payee(seller)
                .amount(settlementAmount)
                .settledAt(null)
                .build();
        settlementRepository.save(activeSettlement);

        Settlement alreadySettled = Settlement.builder()
                .payee(seller)
                .amount(30000L)
                .settledAt(LocalDateTime.now().minusMonths(1))
                .build();
        settlementRepository.save(alreadySettled);

        // [Before] 실행 전 지갑 잔액 조회
        PaymentUser systemUser = paymentUserRepository.findByEmail(CashPolicy.SYSTEM_HOLDING_USER_EMAIL).orElseThrow();
        Wallet systemWalletBefore = paymentSupport.findSystemWallet().orElseThrow();
        Wallet sellerWalletBefore = paymentSupport.findWalletByUserId(seller.getId()).orElseThrow();

        long prevSystemBalance = systemWalletBefore.getBalance();
        long prevSellerBalance = sellerWalletBefore.getBalance();

        // When
        int processedCount = completeUseCase.completeSettlementsChunk(10);

        // Then
        assertThat(processedCount).isEqualTo(1);

        // 1. 정산 상태(settledAt) 변경 확인
        // (findById는 1차 캐시에서 가져오므로 flush 전이라도 변경된 객체를 가져옴)
        Settlement result = settlementRepository.findById(activeSettlement.getId()).orElseThrow();
        assertThat(result.getSettledAt()).isNotNull();

        // [중요 수정] DB 반영(Flush) 후 캐시 비우기(Clear)
        // Flush를 해야 Update 쿼리가 나가고, Clear를 해야 DB에서 다시 조회함
        em.flush();
        em.clear();

        // 2. 지갑 잔액 변동 확인
        Wallet systemWalletAfter = paymentSupport.findSystemWallet().orElseThrow();
        Wallet sellerWalletAfter = paymentSupport.findWalletByUserId(seller.getId()).orElseThrow();

        assertThat(systemWalletAfter.getBalance()).isEqualTo(prevSystemBalance - settlementAmount);
        assertThat(sellerWalletAfter.getBalance()).isEqualTo(prevSellerBalance + settlementAmount);
    }

    @Test
    @DisplayName("[실패] 시스템 지갑 잔액 부족 시 정산 실패 및 롤백되어야 한다")
    void completeSettlementChunk_Fail_InsufficientBalance() {
        // Given
        Wallet systemWallet = paymentSupport.findSystemWallet().orElseThrow();

        ReflectionTestUtils.setField(systemWallet, "balance", 0L);
        walletRepository.save(systemWallet);
        walletRepository.flush(); // 즉시 반영

        Settlement activeSettlement = Settlement.builder()
                .payee(seller)
                .amount(50000L)
                .settledAt(null)
                .build();
        settlementRepository.save(activeSettlement);

        // When & Then
        assertThatThrownBy(() ->
                completeUseCase.completeSettlementsChunk(10)
        );

        em.clear();

        // Verify Rollback
        Settlement result = settlementRepository.findById(activeSettlement.getId()).orElseThrow();
        assertThat(result.getSettledAt()).isNull();
    }

    // --- Helper Methods ---

    private SettlementCandidatedItem createCandidateItem(SettlementUser payee, SettlementUser payer, Long amount, LocalDateTime date, String relNo) {
        SettlementCandidatedItem item = SettlementCandidatedItem.builder()
                .settlementEventType(SettlementEventType.정산__상품판매_대금)
                .relTypeCode("OrderItem")
                .relNo(relNo)
                .paymentDate(date)
                .payee(payee)
                .payer(payer)
                .amount(amount)
                .build();
        return settlementCandidatedItemRepository.save(item);
    }

    private Settlement createActiveSettlement(SettlementUser payee) {
        Settlement settlement = Settlement.builder()
                .payee(payee)
                .amount(0L)
                .settledAt(null)
                .build();
        return settlementRepository.save(settlement);
    }

    private SettlementUser createSettlementUserIfAbsent(Long userId, String nickname, String email) {
        return settlementUserRepository.findByEmail(email)
                .or(() -> settlementUserRepository.findById(userId))
                .orElseGet(() -> {
                    SettlementUser user = SettlementUser.builder()
                            .id(userId)
                            .nickname(nickname + "-" + userId)
                            .email(email)
                            .password("encodedPassword")
                            .phoneNumber("010-0000-0000")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(null)
                            .deletedAt(null)
                            .status("ACTIVE")
                            .build();
                    return settlementUserRepository.save(user);
                });
    }

    private void createPaymentUserAndWalletIfAbsent(Long userId, String email) {
        PaymentUser user = paymentUserRepository.findByEmail(email)
                .or(() -> paymentUserRepository.findById(userId))
                .orElseGet(() -> {
                    PaymentUser newUser = PaymentUser.builder()
                            .id(userId)
                            .nickname("PayUser-" + userId)
                            .email(email)
                            .password("encodedPassword")
                            .phoneNumber("010-0000-0000")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(null)
                            .deletedAt(null)
                            .status("ACTIVE")
                            .build();
                    return paymentUserRepository.save(newUser);
                });

        Wallet wallet = paymentSupport.findWalletByUserId(user.getId())
                .orElseGet(() -> {
                    Wallet newWallet = Wallet.builder()
                            .paymentUser(user)
                            .balance(0L)
                            .build();
                    return walletRepository.save(newWallet);
                });

        ReflectionTestUtils.setField(wallet, "balance", 100_000_000L);
        walletRepository.save(wallet);
    }
}