package com.fourtune.api.infrastructure.kafka.settlement;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SettlementEventPayload {
    private String eventType;
    private Long aggregateId;
    private JsonNode data;
}
