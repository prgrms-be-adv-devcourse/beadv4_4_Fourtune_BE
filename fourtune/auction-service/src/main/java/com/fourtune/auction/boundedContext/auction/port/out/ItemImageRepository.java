package com.fourtune.auction.boundedContext.auction.port.out;

import com.fourtune.auction.boundedContext.auction.domain.entity.ItemImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ItemImageRepository extends JpaRepository<ItemImage, Long> {
    
    List<ItemImage> findByAuctionItemIdOrderByDisplayOrder(Long auctionItemId);
    
    Optional<ItemImage> findByAuctionItemIdAndIsThumbnailTrue(Long auctionItemId);
    
    void deleteByAuctionItemId(Long auctionItemId);
}
