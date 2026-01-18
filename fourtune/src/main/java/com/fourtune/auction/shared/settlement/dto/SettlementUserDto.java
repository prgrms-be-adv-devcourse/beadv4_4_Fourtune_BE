package com.fourtune.auction.shared.settlement.dto;

import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class SettlementUserDto {
    private Long id;
    private String email;
    private String nickname;
    private String phoneNumber;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;
}
