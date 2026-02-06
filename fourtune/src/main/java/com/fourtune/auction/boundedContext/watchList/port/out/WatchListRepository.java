package com.fourtune.auction.boundedContext.watchList.port.out;

import com.fourtune.auction.boundedContext.watchList.domain.WatchList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WatchListRepository extends JpaRepository<WatchList, Long> {

    boolean existsByUserIdAndAuctionItemId(Long userId, Long auctionItemId);
    void deleteByUserIdAndAuctionItemId(Long userId, Long auctionItemId);

    // 1. 기본 LAZY 로딩 (N+1 문제 발생 가능)
    List<WatchList> findAllByUserId(Long userId);

    // 2. Fetch Join 방식 - 한 번의 쿼리로 연관 엔티티 함께 조회
    @Query("SELECT w FROM WatchList w " +
           "JOIN FETCH w.user " +
           "JOIN FETCH w.auctionItem " +
           "WHERE w.user.id = :userId")
    List<WatchList> findAllByUserIdWithFetchJoin(@Param("userId") Long userId);

    // 3. EntityGraph 방식 - 선언적으로 즉시 로딩 설정
    @EntityGraph(attributePaths = {"user", "auctionItem"})
    List<WatchList> findWithGraphByUserId(Long userId);

    @Query("SELECT w.user.id FROM WatchList w WHERE w.auctionItem.id = :auctionItemId")
    List<Long> findAllByAuctionItemId(@Param("auctionItemId") Long auctionItemId);

    Optional<WatchList> findByUserIdAndAuctionItemId(Long userId, Long auctionItemId);
}
