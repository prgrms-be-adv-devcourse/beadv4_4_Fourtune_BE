package com.fourtune.shared.watchList.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WatchListAuctionStartedEvent {
    private final List<Long> users;
    private final Long auctionItemId;
    private final String auctionTitle;
}
