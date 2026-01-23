package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementPolicy;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementCandidatedItemRepository;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementUserRepository;
import com.fourtune.auction.shared.payment.constant.CashPolicy;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AddSettlementCandidatedItemsUseCaseTest {

    @Autowired
    private AddSettlementCandidatedItemsUseCase addSettlementCandidatedItemsUseCase;

    @Autowired
    private SettlementCandidatedItemRepository settlementCandidatedItemRepository;

    @Autowired
    private SettlementUserRepository settlementUserRepository;

    // 테스트용 유저 ID 및 상수
    final Long BUYER_ID = 10L;
    final Long SELLER_ID = 20L;
    final Long ITEM_PRICE = 10000L; // 10,000원

    @BeforeEach
    void setUp() {
        // 1. 일반 유저(구매자, 판매자) 생성 - 중복 방지 로직 적용
        createSettlementUserIfAbsent(BUYER_ID, "buyer", "buyer@test.com");
        createSettlementUserIfAbsent(SELLER_ID, "seller", "seller@test.com");

        // 2. 시스템과 플랫폼 유저 생성 - 중복 방지 로직 적용
        // 이미 DB에 존재할 경우(data.sql 등) Insert 하지 않고 넘어갑니다.
        createSettlementUserIfAbsent(9998L, "platform", CashPolicy.PLATFORM_REVENUE_USER_EMAIL);
        createSettlementUserIfAbsent(9999L, "system", CashPolicy.SYSTEM_HOLDING_USER_EMAIL);
    }

    @Test
    @DisplayName("[성공] 주문 DTO가 들어오면 판매자 대금과 플랫폼 수수료 2개의 정산 후보가 생성된다")
    void addSettlementCandidatedItems_Success() {
        // Given: 주문 정보 생성 (10,000원 짜리 상품)
        OrderDto.OrderItem orderItem = OrderDto.OrderItem.builder()
                .itemId(100L)
                .sellerId(SELLER_ID)
                .price(ITEM_PRICE)
                .itemName("테스트 상품")
                .build();

        OrderDto orderDto = OrderDto.builder()
                .orderId(500L)
                .orderNo("ORD-TEST-001")
                .userId(BUYER_ID) // 구매자
                .paymentDate(LocalDateTime.now())
                .items(List.of(orderItem))
                .build();

        // When: 유스케이스 실행
        addSettlementCandidatedItemsUseCase.addSettlementCandidatedItems(orderDto);

        // Then: 검증
        List<SettlementCandidatedItem> items = settlementCandidatedItemRepository.findAll();

        // 1. 총 2개의 후보 아이템이 생겨야 함 (판매대금 + 수수료)
        assertThat(items).hasSize(2);

        // 2. 수수료 계산 (정책에 따라 다름, 여기선 로직 역산)
        long commissionRate = SettlementPolicy.COMMISSION_RATE.getValue(); // 예: 10
        long expectedCommission = (ITEM_PRICE * commissionRate) / 100L;    // 1000원
        long expectedSellerAmount = ITEM_PRICE - expectedCommission;       // 9000원

        // 3. 판매자 대금 아이템 검증
        SettlementCandidatedItem sellerItem = items.stream()
                .filter(i -> i.getSettlementEventType() == SettlementEventType.정산__상품판매_대금)
                .findFirst()
                .orElseThrow();

        assertThat(sellerItem.getPayee().getId()).isEqualTo(SELLER_ID);
        assertThat(sellerItem.getPayer().getId()).isEqualTo(BUYER_ID);
        assertThat(sellerItem.getAmount()).isEqualTo(expectedSellerAmount);
        assertThat(sellerItem.getRelNo()).isEqualTo("ORD-TEST-001");

        // 4. 플랫폼 수수료 아이템 검증
        SettlementCandidatedItem platformItem = items.stream()
                .filter(i -> i.getSettlementEventType() == SettlementEventType.정산__상품판매_수수료)
                .findFirst()
                .orElseThrow();

        // 플랫폼 유저 ID 검증 (setUp에서 생성한 ID와 일치해야 함)
        // 만약 SettlementUser에 ID 외에 구분자가 있다면 그것으로 검증
        assertThat(platformItem.getPayee().getEmail()).isEqualTo(CashPolicy.PLATFORM_REVENUE_USER_EMAIL); // 혹은 플랫폼 유저 확인 로직
        assertThat(platformItem.getAmount()).isEqualTo(expectedCommission);
    }

    // --- Helper Methods ---

    /**
     * 유저가 DB에 존재하지 않을 때만 생성하는 메서드 (Idempotent)
     */
    private void createSettlementUserIfAbsent(Long userId, String name, String email) {
        // 1. 이메일로 중복 확인 (Unique Index 충돌 방지)
        // (SettlementUserRepository에 findByEmail 메서드가 있다고 가정)
        if (settlementUserRepository.findByEmail(email).isPresent()) {
            return;
        }

        // 2. ID로 중복 확인 (PK 충돌 방지)
        if (settlementUserRepository.findById(userId).isPresent()) {
            return;
        }

        // 3. 없으면 새로 생성 및 저장
        SettlementUser user = SettlementUser.builder()
                .id(userId)
                .nickname(name + "-" + userId)
                .email(email)
                .password("encodedPassword") // Not null 조건 대비 더미 값
                .phoneNumber("010-0000-0000") // Not null 조건 대비 더미 값
                .createdAt(LocalDateTime.now())
                .updatedAt(null)
                .deletedAt(null)
                .status("ACTIVE")
                .build();
        settlementUserRepository.save(user);
    }

}