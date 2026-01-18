package com.fourtune.auction.boundedContext.settlement.domain.entity;

import com.fourtune.auction.shared.user.domain.BaseUser;
import com.fourtune.auction.shared.user.domain.ReplicaUser;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "SETTLEMENT_USER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SettlementUser extends ReplicaUser{
    public SettlementUser(Long id,
                          String email,
                          String nickname,
                          String password,
                          String phoneNumber,
                          LocalDateTime createdAt,
                          LocalDateTime updatedAt,
                          LocalDateTime deletedAt){
        super(id, email, nickname, password, phoneNumber, createdAt, updatedAt, deletedAt);
    }

    //TODO: 기본 유저를 2개 만들기
    // SYSTEM_HOLDING     // [보관용] 결제금 임시 저장
    // PLATFORM_REVENUE      // 플랫폼 매출
}
