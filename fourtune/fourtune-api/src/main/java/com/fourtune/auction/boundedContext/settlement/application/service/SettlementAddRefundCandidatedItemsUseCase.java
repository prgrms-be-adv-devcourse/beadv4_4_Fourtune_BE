package com.fourtune.auction.boundedContext.settlement.application.service;

import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementEventType;
import com.fourtune.auction.boundedContext.settlement.domain.constant.SettlementPolicy;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementCandidatedItem;
import com.fourtune.auction.boundedContext.settlement.domain.entity.SettlementUser;
import com.fourtune.auction.boundedContext.settlement.port.out.SettlementCandidatedItemRepository;
import com.fourtune.shared.payment.dto.RefundDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class SettlementAddRefundCandidatedItemsUseCase {

    private final SettlementCandidatedItemRepository settlementCandidatedItemRepository;
    private final SettlementSupport settlementSupport;

    /**
     * 환불 시 정산 후보 생성
     * - 판매자가 구매자에게 판매 대금 반환
     * - 플랫폼이 구매자에게 수수료 반환
     */
    public void addRefundSettlementCandidatedItems(RefundDto dto) {
        log.info("[Settlement] 환불 정산 후보 생성 시작: refundId={}, orderId={}",
                dto.getRefundId(), dto.getOrderId());

        for (RefundDto.RefundItem item : dto.getItems()) {
            makeRefundSettlementCandidatedItemPair(dto, item);
        }

        log.info("[Settlement] 환불 정산 후보 생성 완료: refundId={}", dto.getRefundId());
    }

    void makeRefundSettlementCandidatedItemPair(RefundDto refund, RefundDto.RefundItem item) {
        SettlementUser buyer = settlementSupport.findUserById(refund.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("구매자를 찾을 수 없습니다: " + refund.getUserId()));
        SettlementUser platform = settlementSupport.findPlatformRevenueUser()
                .orElseThrow(() -> new IllegalStateException("플랫폼 정산 계정을 찾을 수 없습니다"));
        SettlementUser seller = settlementSupport.findUserById(item.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("판매자를 찾을 수 없습니다: " + item.getSellerId()));

        // 1. 판매자 → 구매자 (판매 대금 반환)
        settlementCandidatedItemRepository.save(
                SettlementCandidatedItem.builder()
                        .settlementEventType(SettlementEventType.환불__상품판매_대금)
                        .relTypeCode("RefundItem")
                        .relId(refund.getAuctionOrderId())  // 경매 주문 ID
                        .relNo(refund.getOrderId())         // 주문 번호 (UUID)
                        .paymentDate(refund.getRefundDate())
                        .payee(buyer)                       // 받는 사람: 구매자
                        .payer(seller)                      // 내는 사람: 판매자
                        .amount(getPriceWithoutCommission(item.getRefundPrice()))  // 환불금 - 수수료
                        .build()
        );

        log.debug("[Settlement] 판매 대금 반환 정산 후보 생성: seller={} -> buyer={}, amount={}",
                seller.getId(), buyer.getId(), getPriceWithoutCommission(item.getRefundPrice()));

        // 2. 플랫폼 → 구매자 (수수료 반환)
        settlementCandidatedItemRepository.save(
                SettlementCandidatedItem.builder()
                        .settlementEventType(SettlementEventType.환불__상품판매_수수료)
                        .relTypeCode("RefundItem")
                        .relId(refund.getAuctionOrderId())
                        .relNo(refund.getOrderId())
                        .paymentDate(refund.getRefundDate())
                        .payee(buyer)                       // 받는 사람: 구매자
                        .payer(platform)                    // 내는 사람: 플랫폼
                        .amount(getCommissionAmount(item.getRefundPrice()))  // 수수료
                        .build()
        );

        log.debug("[Settlement] 수수료 반환 정산 후보 생성: platform -> buyer={}, amount={}",
                buyer.getId(), getCommissionAmount(item.getRefundPrice()));
    }

    Long getCommissionAmount(Long refundPrice) {
        return (refundPrice * SettlementPolicy.COMMISSION_RATE.getValue()) / 100L;
    }

    Long getPriceWithoutCommission(Long refundPrice) {
        return refundPrice - getCommissionAmount(refundPrice);
    }
}
