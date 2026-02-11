package com.fourtune.auction.shared.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@Getter
public class SettlementDto {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long payeeId;
    private String payeeEmail;
    private LocalDateTime settledAt;
    private Long amount;
}
