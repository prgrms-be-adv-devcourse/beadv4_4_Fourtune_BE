package com.fourtune.auction.boundedContext.auction.port.out;

import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * 사용자 ID로 장바구니 조회
     */
    Optional<Cart> findByUserId(Long userId);
    
    /**
     * 사용자 ID로 장바구니 조회 (items 포함, LazyInitializationException 방지)
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);
    
    /**
     * 사용자의 장바구니 존재 여부
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 사용자 ID로 장바구니 삭제
     */
    void deleteByUserId(Long userId);
}
