package com.fourtune.shared.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SettlementDto {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long payeeId;
    private String payeeEmail;
    private LocalDateTime settledAt;
    private Long amount;
    private String auctionTitle;
}
