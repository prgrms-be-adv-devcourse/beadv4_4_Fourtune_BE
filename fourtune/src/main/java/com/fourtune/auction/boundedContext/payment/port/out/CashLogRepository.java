package com.fourtune.auction.boundedContext.payment.port.out;

import com.fourtune.auction.boundedContext.payment.domain.entity.CashLog;
import com.fourtune.auction.boundedContext.payment.domain.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashLogRepository extends JpaRepository<CashLog, Long> {
    List<CashLog> findAllByWallet(Wallet wallet);
}
