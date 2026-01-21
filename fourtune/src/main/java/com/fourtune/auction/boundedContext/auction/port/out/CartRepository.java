package com.fourtune.auction.boundedContext.auction.port.out;

import com.fourtune.auction.boundedContext.auction.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    
    /**
     * 사용자 ID로 장바구니 조회
     */
    Optional<Cart> findByUserId(Long userId);
    
    /**
     * 사용자의 장바구니 존재 여부
     */
    boolean existsByUserId(Long userId);
    
    /**
     * 사용자 ID로 장바구니 삭제
     */
    void deleteByUserId(Long userId);
}
