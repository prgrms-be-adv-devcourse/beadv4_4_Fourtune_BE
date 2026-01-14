package com.fourtune.auction.boundedContext.cash.domain.entity;

import com.fourtune.auction.global.common.BaseIdAndTime;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.PERSIST;
import static jakarta.persistence.CascadeType.REMOVE;

@Builder
@AllArgsConstructor
@Entity
@Table(name = "CASH_WALLET")
@NoArgsConstructor
@Getter
public class Wallet extends BaseIdAndTime {

    @ManyToOne(fetch = FetchType.LAZY)
    private CashUser user;

    @Getter
    private long balance;

    @OneToMany(mappedBy = "wallet", cascade = {PERSIST, REMOVE}, orphanRemoval = true)
    private List<CashLog> cashLogs = new ArrayList<>();

}