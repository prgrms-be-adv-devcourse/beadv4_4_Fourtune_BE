package com.fourtune.auction.boundedContext.auction.port.out;

import com.fourtune.auction.boundedContext.auction.domain.constant.CartItemStatus;
import com.fourtune.auction.boundedContext.auction.domain.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * 장바구니 ID로 아이템 목록 조회
     */
    List<CartItem> findByCartId(Long cartId);
    
    /**
     * 장바구니의 특정 상태 아이템 목록 조회
     */
    List<CartItem> findByCartIdAndStatus(Long cartId, CartItemStatus status);
    
    /**
     * 경매 ID로 장바구니 아이템 조회 (모든 장바구니에서)
     */
    List<CartItem> findByAuctionId(Long auctionId);
    
    /**
     * 경매 ID와 상태로 장바구니 아이템 조회
     */
    List<CartItem> findByAuctionIdAndStatus(Long auctionId, CartItemStatus status);
    
    /**
     * 특정 장바구니에서 특정 경매 아이템 조회
     */
    Optional<CartItem> findByCartIdAndAuctionId(Long cartId, Long auctionId);
    
    /**
     * 특정 장바구니에서 특정 경매의 활성 아이템 조회
     */
    Optional<CartItem> findByCartIdAndAuctionIdAndStatus(
        Long cartId, 
        Long auctionId, 
        CartItemStatus status
    );
    
    /**
     * 장바구니에 특정 경매 아이템이 존재하는지 확인
     */
    boolean existsByCartIdAndAuctionId(Long cartId, Long auctionId);
    
    /**
     * 장바구니의 활성 아이템 개수
     */
    long countByCartIdAndStatus(Long cartId, CartItemStatus status);
    
    /**
     * 장바구니 ID로 모든 아이템 삭제
     */
    void deleteByCartId(Long cartId);
    
    /**
     * 특정 경매의 모든 장바구니 아이템을 만료 처리
     * (경매 종료 시 호출)
     */
    @Modifying
    @Query("UPDATE CartItem ci SET ci.status = 'EXPIRED' " +
           "WHERE ci.auctionId = :auctionId " +
           "AND ci.status = 'ACTIVE'")
    void expireActiveItemsByAuctionId(@Param("auctionId") Long auctionId);
    
    /**
     * 특정 장바구니의 구매 완료 아이템 삭제
     */
    @Modifying
    @Query("DELETE FROM CartItem ci " +
           "WHERE ci.cart.id = :cartId " +
           "AND ci.status = 'PURCHASED'")
    void deletePurchasedItemsByCartId(@Param("cartId") Long cartId);
    
    /**
     * 특정 장바구니의 만료된 아이템 삭제
     */
    @Modifying
    @Query("DELETE FROM CartItem ci " +
           "WHERE ci.cart.id = :cartId " +
           "AND ci.status = 'EXPIRED'")
    void deleteExpiredItemsByCartId(@Param("cartId") Long cartId);
}
