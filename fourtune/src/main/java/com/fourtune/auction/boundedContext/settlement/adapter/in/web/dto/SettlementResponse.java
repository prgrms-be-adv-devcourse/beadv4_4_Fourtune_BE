package com.fourtune.auction.boundedContext.settlement.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettlementResponse {
    private Long id;                // 정산서 ID
    private Long payeeId;           // 정산 받는 사람 ID
    private String payeeEmail;      // 정산 받는 사람 이메일
    private Long totalAmount;       // 정산 총액
    private LocalDateTime settledAt;// 정산 완료 일시 (null이면 예정)
    private LocalDateTime createdAt;// 정산 생성 일시
    private LocalDateTime updatedAt;// 최종 수정 일시
    private List<Item> items;       // 정산 상세 항목 리스트

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private Long itemId;
        private String eventType;   // 정산 타입 (SALE, COMMISSION 등)
        private String relTypeCode; // 관련 데이터 타입 (ORDER 등)
        private Long relId;         // 관련 데이터 ID
        private Long amount;        // 금액
        private String payerName;   // 송금자 이름 (있을 경우)
        private LocalDateTime paymentDate; // 결제 발생 일시
        private List<Item> items;       // 정산 상세 항목 리스트
    }
}
