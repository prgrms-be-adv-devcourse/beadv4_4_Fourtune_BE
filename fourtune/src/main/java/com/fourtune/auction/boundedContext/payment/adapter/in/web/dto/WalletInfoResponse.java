package com.fourtune.auction.boundedContext.payment.adapter.in.web.dto;

import com.fourtune.auction.boundedContext.payment.domain.entity.CashLog;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class WalletInfoResponse {

    private Long currentBalance;        // 현재 잔액
    private List<CashLogDto> history;   // 캐시 사용/충전 내역

    /**
     * 지갑 엔티티와 캐시 로그 리스트를 받아 하나의 응답으로 변환
     */
    public static WalletInfoResponse of(Wallet wallet, List<CashLog> cashLogs) {
        return WalletInfoResponse.builder()
                .currentBalance(wallet.getBalance())
                .history(cashLogs.stream()
                        .map(CashLogDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * 내부용 DTO: 리스트 안의 개별 내역 정보
     */
    @Getter
    @Builder
    public static class CashLogDto {
        private Long id;
        private Long amount;        // 변동 금액 (+1000, -500)
        private String eventType;   // 충전, 사용, 환불 등
        private String description; // 적요 (ex: "OOO 경매 낙찰")
        private LocalDateTime createdAt;

        public static CashLogDto from(CashLog log) {
            return CashLogDto.builder()
                    .id(log.getId())
                    .amount(log.getAmount()) // CashLog 엔티티 필드명에 맞게 조정 필요
                    .eventType(log.getEventType().name()) // Enum -> String
                    .createdAt(log.getCreatedAt())
                    .build();
        }
    }
}