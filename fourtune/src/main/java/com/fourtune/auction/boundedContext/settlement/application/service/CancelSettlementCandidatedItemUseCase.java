package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementPolicy;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementCandidatedItemRepository;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CancelSettlementCandidatedItemUseCase {

    private final SettlementCandidatedItemRepository settlementCandidatedItemRepository;
    private final SettlementSupport settlementSupport;

    /**
     * 주문 취소(환불)에 따른 마이너스 정산 후보 생성
     */
    public void cancelSettlementCandidatedItem(OrderDto orderDto){
        // 주문에 포함된 모든 아이템에 대해 마이너스 정산 데이터 생성
        for(OrderDto.OrderItem item : orderDto.getItems()){
            createRefundSettlementCandidates(orderDto, item);
        }
    }

    private void createRefundSettlementCandidates(OrderDto order, OrderDto.OrderItem item){
        SettlementUser buyer = settlementSupport.findUserById(order.getUserId()).orElseThrow();
        SettlementUser platform = settlementSupport.findPlatformRevenueUser().orElseThrow();
        SettlementUser seller = settlementSupport.findUserById(item.getSellerId()).orElseThrow();

        // 환불 발생 시점 (지금)
        LocalDateTime refundDate = LocalDateTime.now();

        // 1. [환불] 판매자 매출 차감 (마이너스 금액)
        settlementCandidatedItemRepository.save(
                SettlementCandidatedItem.builder()
                        .settlementEventType(SettlementEventType.환불__상품판매_대금)
                        .relTypeCode("OrderItem")
                        .relId(order.getOrderId())
                        .relNo(order.getOrderNo())
                        .paymentDate(refundDate) // 결제일이 아닌 '환불 시점' 기준
                        .payee(seller) // 돈을 받을 예정이었던 판매자에게서
                        .payer(buyer)
                        .amount( -getPriceWithoutCommission(item.getPrice()) ) // 음수 처리
                        .build()
        );

        // 2. [환불] 플랫폼 수수료 차감 (마이너스 금액)
        settlementCandidatedItemRepository.save(
                SettlementCandidatedItem.builder()
                        .settlementEventType(SettlementEventType.환불__상품판매_수수료)
                        .relTypeCode("OrderItem")
                        .relId(order.getOrderId())
                        .relNo(order.getOrderNo())
                        .paymentDate(refundDate) // 원 결제일이 아닌 '환불 시점' 기준
                        .payee(platform) // 수수료를 받을 예정이었던 플랫폼에게서
                        .payer(buyer)
                        .amount( -getCommissionAmount(item.getPrice()) ) // 음수 처리
                        .build()
        );
    }

    private Long getCommissionAmount(Long orderItemPrice){
        return (orderItemPrice * SettlementPolicy.COMMISSION_RATE.getValue()) / 100L;
    }

    private Long getPriceWithoutCommission(Long orderItemPrice){
        return orderItemPrice - getCommissionAmount(orderItemPrice);
    }
}