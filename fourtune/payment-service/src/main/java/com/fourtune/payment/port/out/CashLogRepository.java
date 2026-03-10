package com.fourtune.payment.port.out;

import com.fourtune.payment.domain.constant.CashEventType;
import com.fourtune.payment.domain.entity.CashLog;
import com.fourtune.payment.domain.entity.Wallet;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CashLogRepository extends JpaRepository<CashLog, Long> {
    List<CashLog> findAllByWallet(Wallet wallet);

    List<CashLog> findCashLogsByPaymentUserIdOrderByIdDesc(Long userId, PageRequest of);

    /** 정산 지급 멱등성 확인 — 동일 settlementId에 대한 지급 이력이 이미 존재하는지 확인 */
    boolean existsByRelTypeCodeAndRelIdAndEventType(String relTypeCode, Long relId, CashEventType eventType);
}
