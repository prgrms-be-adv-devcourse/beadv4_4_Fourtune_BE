package com.fourtune.auction.boundedContext.payment.application.service;

import com.fourtune.auction.boundedContext.payment.domain.vo.PaymentExecutionResult;
import com.fourtune.auction.boundedContext.payment.port.out.AuctionPort;
import com.fourtune.auction.boundedContext.payment.port.out.PaymentGatewayPort;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import com.fourtune.auction.shared.payment.event.PaymentFailedEvent;
import com.fourtune.auction.global.eventPublisher.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfirmUseCase {

        private final PaymentGatewayPort paymentGatewayPort;     // 외부: Toss
        private final AuctionPort auctionPort;                   // 외부/내부: 경매 모듈 정보 조회
        private final PaymentCashCompleteUseCase paymentCashCompleteUseCase;  // 내부: 지갑 로직 (기존 로직 활용)
        private final EventPublisher eventPublisher;

        public PaymentExecutionResult confirmPayment(String paymentKey, Long orderId, Long amount) {

                // 1. [외부] Toss 결제 승인 요청 (트랜잭션 밖에서 수행 권장)
                // 여기가 실패하면 그냥 에러 던지고 끝남 (돈 안 나감)
                PaymentExecutionResult result = paymentGatewayPort.confirm(paymentKey, String.valueOf(orderId), amount);

                // 2. [내부] 시스템 검증 및 자산 이동 (보상 트랜잭션 필요 구간)
                try {
                        processInternalSystemLogic(orderId, amount);
                } catch (Exception e) {
                        log.error("내부 시스템 처리 실패. 결제 승인 취소를 진행합니다. orderId={}, error={}", orderId, e.getMessage());

                        // 3. [보상 트랜잭션] 내부 로직 실패 시 Toss 결제 취소
                        paymentGatewayPort.cancel(paymentKey, "System Logic Failed: " + e.getMessage());

                        // 실패 이벤트 발행
                        eventPublisher.publish(new PaymentFailedEvent(
                                "500", "내부 시스템 오류로 결제가 취소되었습니다.", null, amount, 0L
                        ));

                        throw e; // 컨트롤러에게 예외 다시 던짐
                }
                return result;
        }

        // 내부 로직은 데이터 정합성을 위해 트랜잭션으로 묶음
        protected void processInternalSystemLogic(Long orderId, Long amount) {
                // 2-1. 경매 주문 정보 확인
                OrderDto orderDto = auctionPort.getOrder(orderId); // AuctionPort 인터페이스 필요

                if (orderDto == null) {
                        throw new IllegalArgumentException("존재하지 않는 주문입니다.");
                }

                if (orderDto.getPrice() != amount) { // 가격 불일치
                        throw new IllegalArgumentException("주문 금액과 결제 금액이 일치하지 않습니다.");
                }

                // 2-2. 지갑 충전 및 시스템 이동 (기존 로직 재사용)
                // 이 안에서 예외가 터지면 위쪽 catch 블록으로 이동 -> Toss Cancel 호출됨
                paymentCashCompleteUseCase.cashComplete(orderDto, amount);
        }
}