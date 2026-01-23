package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.payment.application.service.PaymentSupport;
import com.fourtune.auction.boundedContext.payment.domain.entity.PaymentUser;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentUserRepository;
import com.fourtune.auction.boundedContext.payment.port.out.WalletRepository;
import com.fourtune.auction.boundedContext.settlement.application.service.CollectSettlementItemChunkUseCase;
import com.fourtune.auction.boundedContext.settlement.application.service.CompleteSettlementChunkUseCase;
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
import org.springframework.test.util.ReflectionTestUtils; // [필수] 강제 값 주입용
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SettlementTest {

    @Autowired CollectSettlementItemChunkUseCase collectUseCase;
    @Autowired CompleteSettlementChunkUseCase completeUseCase;

    @Autowired SettlementRepository settlementRepository;
    @Autowired SettlementCandidatedItemRepository settlementCandidatedItemRepository;
    @Autowired SettlementUserRepository settlementUserRepository;

    // [추가] 결제 모듈용 Repository
    @Autowired PaymentUserRepository paymentUserRepository;
    @Autowired WalletRepository walletRepository;
    @Autowired PaymentSupport paymentSupport;
    @Autowired EntityManager em;

    // 테스트 데이터
    SettlementUser seller;
    SettlementUser buyer;
    SettlementUser platform;

    @BeforeEach
    void setUp() {
        // [중요 1] 테스트 시작 전 더티 데이터 정리
        settlementCandidatedItemRepository.deleteAll();

        // 1. 정산 모듈 유저 세팅 (SettlementUser)
        seller = createSettlementUserIfAbsent(10L, "Seller");
        buyer = createSettlementUserIfAbsent(20L, "Buyer");
        platform = createSettlementUserIfAbsent(9999L, "Platform");

        // [중요 2] 결제 모듈용 지갑 데이터 생성 (NOT NULL 필드 포함)

        // (1) 판매자 지갑 (돈 받을 사람)
        createPaymentUserAndWalletIfAbsent(seller.getId(), seller.getEmail());

        // (2) 시스템 지갑 (돈 줄 사람 - CashPolicy 이메일 필수)
        createPaymentUserAndWalletIfAbsent(8888L, CashPolicy.SYSTEM_HOLDING_USER_EMAIL);

        // (3) 플랫폼 지갑 (수수료 받을 사람 - CashPolicy 이메일 필수)
        createPaymentUserAndWalletIfAbsent(8889L, CashPolicy.PLATFORM_REVENUE_USER_EMAIL);
    }

    @Test
    @DisplayName("[수집] 구매확정 기간이 지난 후보 아이템들을 모아서 정산서(Settlement)에 연결한다")
    void collectSettlementItemChunk_Success() {
        // Given
        Settlement activeSettlement = createActiveSettlement(seller);

        long waitingDays = SettlementPolicy.SETTLEMENT_WAITING_DAYS.getValue();
        // 정산 대상 (기간 지남)
        LocalDateTime pastDate = LocalDateTime.now().minusDays(waitingDays + 1);

        SettlementCandidatedItem targetItem = createCandidateItem(
                seller, buyer, 10000L, pastDate, "ITEM_001"
        );

        // 비대상 (아직 안 지남)
        SettlementCandidatedItem futureItem = createCandidateItem(
                seller, buyer, 5000L, LocalDateTime.now().minusDays(0), "ITEM_002"
        );

        // When
        int processedCount = collectUseCase.collectSettlementItemChunk(10);

        // Then
        assertThat(processedCount).isEqualTo(1); // 대상인 1개만 처리되어야 함

        // 1. 대상 아이템 검증
        SettlementCandidatedItem updatedTarget = settlementCandidatedItemRepository.findById(targetItem.getId()).orElseThrow();
        assertThat(updatedTarget.getSettlementItem()).isNotNull();
        assertThat(updatedTarget.getSettlementItem().getAmount()).isEqualTo(10000L);

        // 2. 비대상 아이템 검증
        SettlementCandidatedItem updatedFuture = settlementCandidatedItemRepository.findById(futureItem.getId()).orElseThrow();
        assertThat(updatedFuture.getSettlementItem()).isNull();
    }

    @Test
    @DisplayName("[완료] 정산되지 않은 정산서를 확정(Complete) 처리하고 정산일시를 기록한다")
    void completeSettlementChunk_Success() {
        // Given
        Settlement activeSettlement = Settlement.builder()
                .payee(seller)
                .amount(50000L) // 정산 받을 금액
                .settledAt(null) // 아직 정산 안 됨
                .build();
        settlementRepository.save(activeSettlement);

        Settlement alreadySettled = Settlement.builder()
                .payee(seller)
                .amount(30000L)
                .settledAt(LocalDateTime.now().minusMonths(1))
                .build();
        settlementRepository.save(alreadySettled);

        // When
        int processedCount = completeUseCase.completeSettlementsChunk(10);

        // Then
        assertThat(processedCount).isEqualTo(1);

        Settlement result = settlementRepository.findById(activeSettlement.getId()).orElseThrow();
        assertThat(result.getSettledAt()).isNotNull(); // 정산 일시가 찍혀야 함
    }

    @Test
    @DisplayName("[실패] 시스템 지갑 잔액 부족 시 정산 실패 및 롤백되어야 한다")
    void completeSettlementChunk_Fail_InsufficientBalance() {
        // Given
        // 1. 시스템 지갑의 잔액을 0원으로 강제 변경 (실패 유도)
        PaymentUser systemUser = paymentUserRepository.findByEmail(CashPolicy.SYSTEM_HOLDING_USER_EMAIL).orElseThrow();
        Wallet systemWallet = paymentSupport.findSystemWallet().orElseThrow();

        // (테스트용으로 강제 값 주입 및 반영)
        ReflectionTestUtils.setField(systemWallet, "balance", 0L);
        walletRepository.save(systemWallet);
        walletRepository.flush(); // 즉시 반영

        // 2. 활성 정산서 생성 (50,000원 지급 필요)
        Settlement activeSettlement = Settlement.builder()
                .payee(seller)
                .amount(50000L)
                .settledAt(null)
                .build();
        settlementRepository.save(activeSettlement);

        // When & Then
        // 잔액 부족으로 인해 예외 발생 확인 (BusinessException 등)
        assertThatThrownBy(() ->
                completeUseCase.completeSettlementsChunk(10)
        ); // 여기서 BusinessException이 터지면 -> Transaction Rollback 발동!

        // Then
        // [중요] 영속성 컨텍스트 비우기!
        // 비우지 않으면, 위 로직에서 객체에 이미 setSettledAt(now) 한 상태가 그대로 조회됨
        em.clear();

        // Verify Rollback
        // 예외가 발생했으므로 트랜잭션이 롤백되어 정산일시(settledAt)가 여전히 null이어야 함
        Settlement result = settlementRepository.findById(activeSettlement.getId()).orElseThrow();
        assertThat(result.getSettledAt()).isNull(); // 롤백 성공 확인
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

    private SettlementUser createSettlementUserIfAbsent(Long userId, String nickname) {
        return settlementUserRepository.findById(userId).orElseGet(() -> {
            SettlementUser user = SettlementUser.builder()
                    .id(userId)
                    .nickname(nickname + "-" + userId)
                    .email(nickname + "@test.com")
                    .password("encodedPassword") // Not null 필드 채우기
                    .phoneNumber("010-0000-0000") // Not null 필드 채우기
                    .createdAt(LocalDateTime.now()) // Not null 필드 채우기
                    .updatedAt(null)
                    .deletedAt(null)
                    .status("ACTIVE")
                    .build();
            return settlementUserRepository.save(user);
        });
    }

    // [추가] 결제 유저 및 지갑 생성 (중복 방지 & 필수 필드 포함)
    private void createPaymentUserAndWalletIfAbsent(Long userId, String email) {
        if (paymentUserRepository.findByEmail(email).isPresent()) {
            return;
        }

        PaymentUser user = PaymentUser.builder()
                .id(userId)
                .nickname("name" + "-" + userId)
                .email(email)
                .password("encodedPassword") // Not null 조건 대비 더미 값
                .phoneNumber("010-0000-0000") // Not null 조건 대비 더미 값
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .deletedAt(null)
                .status("ACTIVE")
                .build();
        paymentUserRepository.save(user);

        Wallet wallet = Wallet.builder()
                .paymentUser(user)
                .balance(100_000_000L) // 1억 충전 (부족하지 않게)
                .build();
        walletRepository.save(wallet);
    }
}