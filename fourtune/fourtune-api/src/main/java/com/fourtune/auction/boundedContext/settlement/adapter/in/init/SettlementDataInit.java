package com.fourtune.auction.boundedContext.settlement.adapter.in.init;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementCandidatedItemRepository;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementUserRepository;
import com.fourtune.core.eventPublisher.EventPublisher;
import com.fourtune.shared.user.dto.UserResponse;
import com.fourtune.shared.user.event.UserJoinedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SettlementDataInit {

    /**
     * [테스트용 토글]
     * true: 서버 시작 시 초기 데이터 생성 실행
     * false: 실행 안 함
     */
    private static final boolean ENABLE_INIT = false;

    // 내부의 별도 컴포넌트를 주입받아 사용 (Self-Invocation 문제 해결)
    private final InitService initService;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (!ENABLE_INIT) {
                log.info("[SettlementDataInit] ENABLE_INIT = false 설정으로 인해 초기 데이터 생성을 건너뜁니다.");
                return;
            }

            log.info("[SettlementDataInit] 초기 데이터 생성 시작 (UserJoinedEvent 기반)");

            // 1. 유저 가입 이벤트 발행 (별도 트랜잭션으로 즉시 커밋)
            initService.createUsers();

            // 2. 정산 후보 아이템 데이터 생성 (유저 조회 후 생성)
            initService.createSettlementItems();

            log.info("[SettlementDataInit] 모든 초기 데이터 생성 완료");
        };
    }

    /**
     * 트랜잭션 분리를 위한 내부 서비스 컴포넌트
     */
    @Component
    @RequiredArgsConstructor
    static class InitService {
        private final SettlementUserRepository settlementUserRepository;
        private final SettlementCandidatedItemRepository settlementCandidatedItemRepository;
        private final EventPublisher eventPublisher;

        // REQUIRES_NEW: 독립적인 트랜잭션 보장 (메서드 종료 시 커밋됨)
        @Transactional(propagation = Propagation.REQUIRES_NEW)
        public void createUsers() {
            publishMockUser(900L, "holding@system.com", "SYSTEM_HOLDING");
            publishMockUser(901L, "revenue@platform.com", "PLATFORM_REVENUE");
            publishMockUser(1L, "buyer@test.com", "buyer_user");
            publishMockUser(2L, "seller@test.com", "seller_user");
            log.info("[InitService] 유저 이벤트 발행 완료 (커밋 예정)");
        }

        // 일반 Transactional: 위 트랜잭션이 커밋된 후 실행되므로 안전하게 조회 가능
        @Transactional
        public void createSettlementItems() {
            // 유저 조회 (없으면 강제 생성 - Fallback)
            SettlementUser holdingUser = findOrForceCreateUser(900L, "holding@system.com", "SYSTEM_HOLDING");
            SettlementUser revenueUser = findOrForceCreateUser(901L, "revenue@platform.com", "PLATFORM_REVENUE");
            SettlementUser payerUser = findOrForceCreateUser(1L, "buyer@test.com", "buyer_user");
            SettlementUser payeeUser = findOrForceCreateUser(2L, "seller@test.com", "seller_user");

            log.info("[InitService] 유저 데이터 4명 확인 및 로드 완료");

            // Case 1: 일반 경매 낙찰 (구매자 -> 판매자)
            SettlementCandidatedItem item1 = SettlementCandidatedItem.builder()
                    .settlementEventType(SettlementEventType.정산__상품판매_대금)
                    .relTypeCode("ORDER")
                    .relId(101L)
                    .paymentDate(LocalDateTime.now().minusDays(1))
                    .payer(payerUser)
                    .payee(payeeUser)
                    .amount(4500L)
                    .build();

            // Case 2: 플랫폼 수수료 (판매자 -> 플랫폼)
            SettlementCandidatedItem item2 = SettlementCandidatedItem.builder()
                    .settlementEventType(SettlementEventType.정산__상품판매_수수료)
                    .relTypeCode("ORDER")
                    .relId(101L)
                    .paymentDate(LocalDateTime.now().minusDays(1))
                    .payer(payerUser)
                    .payee(revenueUser)
                    .amount(500L)
                    .build();

            // Case 3: 배송비 정산 (구매자 -> 판매자)
            SettlementCandidatedItem item3 = SettlementCandidatedItem.builder()
                    .settlementEventType(SettlementEventType.정산__상품판매_대금)
                    .relTypeCode("ORDER")
                    .relId(101L)
                    .paymentDate(LocalDateTime.now().minusDays(1))
                    .payer(payerUser)
                    .payee(payeeUser)
                    .amount(10000L)
                    .build();

            // Case 4: 낙찰 취소 위약금
            SettlementCandidatedItem item4 = SettlementCandidatedItem.builder()
                    .settlementEventType(SettlementEventType.정산__상품판매_수수료)
                    .relTypeCode("ORDER")
                    .relId(105L)
                    .paymentDate(LocalDateTime.now().minusHours(5))
                    .payer(payerUser)
                    .payee(revenueUser)
                    .amount(1000L)
                    .build();

            settlementCandidatedItemRepository.saveAll(Arrays.asList(item1, item2, item3, item4));
            log.info("[InitService] 정산 후보 아이템 4건 저장 완료");
        }

        private void publishMockUser(Long id, String email, String nickname) {
            if (settlementUserRepository.existsById(id)) {
                return;
            }

            UserResponse userResponse = new UserResponse(
                    id,
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    email,
                    nickname,
                    "ACTIVE",
                    "ROLE_USER"
            );

            eventPublisher.publish(new UserJoinedEvent(userResponse));
        }

        // 재시도 로직 간소화: 트랜잭션 분리로 인해 Thread.sleep 없이도 조회가 가능하지만,
        // 혹시 모를 상황을 대비해 없으면 생성하는 로직(Fallback)은 유지
        private SettlementUser findOrForceCreateUser(Long id, String email, String nickname) {
            return settlementUserRepository.findById(id).orElseGet(() -> {
                log.warn("User({}) not found after event publish. Force saving...", id);
                SettlementUser user = new SettlementUser(
                        id,
                        email,
                        nickname,
                        "password",
                        "010-0000-0000",
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        null,
                        "ACTIVE"
                );
                return settlementUserRepository.save(user);
            });
        }
    }
}
