package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.auction.domain.entity.Order;
import com.fourtune.auction.boundedContext.settlement.adapter.in.web.dto.SettlementDto;
import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementPolicy;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementCandidatedItemRepository;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementUserRepository;
import com.fourtune.auction.shared.payment.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AddSettlementCandidatedItemsUseCase {
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

        settlementCandidatedItemRepository.save(
                SettlementCandidatedItem.builder()
                        .settlementEventType(SettlementEventType.정산__상품판매_대금)
                        .relTypeCode("OrderItem")
                        .relId(item.getItemId())
                        .paymentDate(order.getPaymentDate())
                        .payee(seller)
                        .payer(buyer)
                        .amount(getPriceWithoutCommission(item.getPrice()))// 판매금 - 수수료
                        .build()
        );

        settlementCandidatedItemRepository.save(
                SettlementCandidatedItem.builder()
                        .settlementEventType(SettlementEventType.정산__상품판매_수수료)
                        .relTypeCode("OrderItem")
                        .relId(item.getItemId())
                        .paymentDate(order.getPaymentDate())
                        .payee(platform)
                        .payer(buyer)
                        .amount(getCommissionAmount(item.getPrice()))// 수수료
                        .build()
        );
    }

    Long getCommissionAmount(Long orderItemPrice){
        return (orderItemPrice * SettlementPolicy.COMMISSION_RATE.getValue()) / 100L;
    }

    Long getPriceWithoutCommission(Long orderItemPrice){
        return orderItemPrice - getCommissionAmount(orderItemPrice);
    }

}
