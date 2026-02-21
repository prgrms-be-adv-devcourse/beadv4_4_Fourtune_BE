package com.fourtune.auction.boundedContext.settlement.domain.entity;

import com.fourtune.shared.settlement.dto.SettlementUserDto;
import com.fourtune.shared.user.domain.ReplicaUser;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SETTLEMENT_USER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SettlementUser extends ReplicaUser{

    @Builder
    public SettlementUser(Long id,
                          String email,
                          String nickname,
                          String password,
                          String phoneNumber,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt,
                          LocalDateTime deletedAt,
                          String status){
        super(id, email, nickname, password, phoneNumber, createdAt, updatedAt, deletedAt, status);
    }

    public SettlementUserDto toDto() {
        return new SettlementUserDto(
                getId(),
                getEmail(),
                getNickname(),
                getPhoneNumber(),
                getCreatedAt(),
                getUpdatedAt(),
                getDeletedAt(),
                getStatus()
        );
    }

}
