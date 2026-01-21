package com.fourtune.auction.boundedContext.search.adapter.in.event;

import com.fourtune.auction.boundedContext.search.application.service.AuctionItemIndexingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuctionItemIndexEventListener {

    private final AuctionItemIndexingHandler indexingHandler;

    // @EventListener
    // public void handle(AuctionItemCreatedEvent e) { ... }
}
