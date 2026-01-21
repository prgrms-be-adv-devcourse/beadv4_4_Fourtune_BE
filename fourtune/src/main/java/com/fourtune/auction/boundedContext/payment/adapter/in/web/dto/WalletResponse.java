package com.fourtune.auction.boundedContext.payment.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fourtune.auction.boundedContext.payment.domain.entity.CashLog;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL) // 값이 null이면 JSON 응답에서 아예 필드를 제외시킴
public class WalletResponse {
    private Long balance;
    private List<CashLogResponse> history;

    // 잔액만
    public static WalletResponse of(Long balance){
        return WalletResponse.builder()
                .balance(balance)
                .build();
    }

    // 잔액 + 내역 모두 포함
    public static WalletResponse of(Long balance, List<CashLog> logs) {
        return WalletResponse.builder()
                .balance(balance)
                .history(
                        logs.stream()
                                .map(CashLogResponse::from)
                                .collect(Collectors.toList())
                )
                .build();
    }

    // 내역만
    public static WalletResponse of(List<CashLog> logs) {
        return WalletResponse.builder()
                .balance(null) // 잔액은 보내지 않음
                .history(
                        logs.stream()
                                .map(CashLogResponse::from)
                                .collect(Collectors.toList())
                )
                .build();
    }

    @Getter
    @Builder
    public static class CashLogResponse {
        private Long id;
        private Long amount;
        private Long balance;
        private String relTypeCode;
        private Long relId;
        private String eventType;
        private LocalDateTime createdAt;

        public static CashLogResponse from(CashLog log) {
            return CashLogResponse.builder()
                    .id(log.getId())
                    .amount(log.getAmount())
                    .balance(log.getBalance())
                    .eventType(log.getEventType().name())
                    .createdAt(log.getCreatedAt())
                    .relTypeCode(log.getRelTypeCode())
                    .relId(log.getRelId())
                    .build();
        }
    }
}
