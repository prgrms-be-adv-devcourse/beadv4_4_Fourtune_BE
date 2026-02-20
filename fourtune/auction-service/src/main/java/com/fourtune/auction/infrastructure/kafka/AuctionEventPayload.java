package com.fourtune.auction.infrastructure.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 경매 Outbox payload 래퍼 (User/Outbox 인터페이스 변경 없이 사용)
 * UseCase에서 append 시 Map으로 이 구조를 저장하고, Handler에서 payload 문자열만 받아 역직렬화할 때 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AuctionEventPayload {

    private String eventType;
    private Long aggregateId;
    private JsonNode data;
}
