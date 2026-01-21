package com.fourtune.auction.boundedContext.watchList.port.out;

import com.fourtune.auction.boundedContext.watchList.domain.WatchListUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WatchListUserRepository extends JpaRepository<WatchListUser, Long> {
}
