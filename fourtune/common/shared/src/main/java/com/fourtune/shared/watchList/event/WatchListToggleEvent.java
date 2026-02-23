package com.fourtune.shared.watchList.event;

public record WatchListToggleEvent(
        Long userId,
        String action,
        ItemData itemData
) {
    public record ItemData(
            Long auctionId,
            String category,
            long price,
            String title
    ) {}
}
