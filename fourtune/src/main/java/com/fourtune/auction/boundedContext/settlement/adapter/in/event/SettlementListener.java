package com.fourtune.auction.boundedContext.settlement.adapter.in.event;

import org.springframework.stereotype.Component;

@Component
public class SettlementListener {
    //todo: member created, modified -> user sync, create settlement

    //todo: payment completed or order completed event -> 정산 후보로 등록

    //todo: settlement completed event -> create payee's new empty settlement
}
