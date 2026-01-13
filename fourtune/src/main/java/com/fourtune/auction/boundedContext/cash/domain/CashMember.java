package com.fourtune.auction.boundedContext.cash.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CASH_MEMBER")
@Getter
@NoArgsConstructor
public class CashMember {
    @Id
    private Long id;
    LocalDateTime createDate;
    LocalDateTime modifyDate;
    String username;
    String password;
    String nickname;
    int activityScore;

}
