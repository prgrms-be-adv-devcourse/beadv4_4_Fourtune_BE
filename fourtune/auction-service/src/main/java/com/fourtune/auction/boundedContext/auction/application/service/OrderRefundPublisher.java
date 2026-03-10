package com.fourtune.auction.boundedContext.auction.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fourtune.auction.infrastructure.kafka.AuctionKafkaProducer;
import com.fourtune.shared.kafka.auction.AuctionEventType;
import com.fourtune.shared.payment.dto.RefundDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 환불 완료 이벤트를 auction-events 토픽에 발행.
 * fourtune-api의 SettlementAuctionKafkaListener가 ORDER_REFUNDED를 수신해 정산 후보를 등록한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "feature.kafka.enabled", havingValue = "true", matchIfMissing = false)
public class OrderRefundPublisher {

    private final AuctionKafkaProducer auctionKafkaProducer;
    private final ObjectMapper objectMapper;

    public void publishOrderRefunded(RefundDto refundDto) {
        try {
            String payload = objectMapper.writeValueAsString(refundDto);
            String key = refundDto.getOrderId() != null ? refundDto.getOrderId() : "refund";
            auctionKafkaProducer.sendSync(key, payload, AuctionEventType.ORDER_REFUNDED.name());
        } catch (JsonProcessingException e) {
            log.error("ORDER_REFUNDED 이벤트 직렬화 실패: orderId={}, error={}",
                    refundDto.getOrderId(), e.getMessage());
            throw new RuntimeException("ORDER_REFUNDED 이벤트 발행 실패", e);
        }
    }
}
