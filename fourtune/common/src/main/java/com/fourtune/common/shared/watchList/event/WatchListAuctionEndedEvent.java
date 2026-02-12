package com.fourtune.common.shared.watchList.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class WatchListAuctionEndedEvent {
    private final List<Long> users;
    private final Long auctionItemId;
}
