package com.fourtune.payment.port.out;

import com.fourtune.payment.domain.entity.CashLog;
import com.fourtune.payment.domain.entity.Wallet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashLogRepository extends JpaRepository<CashLog, Long> {
    List<CashLog> findAllByWallet(Wallet wallet);

    List<CashLog> findCashLogsByPaymentUserIdOrderByIdDesc(Long userId, PageRequest of);
}
