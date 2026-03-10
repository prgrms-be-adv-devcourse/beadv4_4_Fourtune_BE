package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementPolicy;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementCandidatedItemRepository;
import com.fourtune.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SettlementAddCandidatedItemsUseCase {
    private final SettlementCandidatedItemRepository settlementCandidatedItemRepository;
    private final SettlementSupport settlementSupport;

    public void addSettlementCandidatedItems(OrderDto dto){
        for(OrderDto.OrderItem item : dto.getItems()){
            makeSettlementCandidatedItemPair(dto, item);
        }
    }

    void makeSettlementCandidatedItemPair(OrderDto order, OrderDto.OrderItem item){
        SettlementUser buyer = settlementSupport.findUserById(order.getUserId()).orElseThrow();
        SettlementUser platform = settlementSupport.findPlatformRevenueUser().orElseThrow();
        SettlementUser seller = settlementSupport.findUserById(item.getSellerId()).orElseThrow();

        saveIgnoreDuplicate(SettlementCandidatedItem.builder()
                .settlementEventType(SettlementEventType.정산__상품판매_대금)
                .relTypeCode("OrderItem")
                .relId(order.getAuctionOrderId())
                .relNo(order.getOrderId())
                .paymentDate(order.getPaymentDate())
                .payee(seller)
                .payer(buyer)
                .amount(getPriceWithoutCommission(item.getPrice()))
                .build());

        saveIgnoreDuplicate(SettlementCandidatedItem.builder()
                .settlementEventType(SettlementEventType.정산__상품판매_수수료)
                .relTypeCode("OrderItem")
                .relId(order.getAuctionOrderId())
                .relNo(order.getOrderId())
                .paymentDate(order.getPaymentDate())
                .payee(platform)
                .payer(buyer)
                .amount(getCommissionAmount(item.getPrice()))
                .build());
    }

    /**
     * [DEFECT-002 수정] Kafka at-least-once 중복 이벤트 방어.
     * SettlementCandidatedItem의 (relNo, settlementEventType) 유니크 제약에 의해
     * 중복 INSERT 시 DataIntegrityViolationException이 발생한다 → 무시하고 넘어간다.
     */
    private void saveIgnoreDuplicate(SettlementCandidatedItem item) {
        try {
            settlementCandidatedItemRepository.saveAndFlush(item);
        } catch (DataIntegrityViolationException e) {
            log.warn("정산 후보 중복 등록 감지 (Kafka 재전송 추정), 스킵: relNo={}, type={}",
                    item.getRelNo(), item.getSettlementEventType());
        }
    }

    Long getCommissionAmount(Long orderItemPrice){
        return (orderItemPrice * SettlementPolicy.COMMISSION_RATE.getValue()) / 100L;
    }

    Long getPriceWithoutCommission(Long orderItemPrice){
        return orderItemPrice - getCommissionAmount(orderItemPrice);
    }

}
