package com.fourtune.auction.boundedContext.user.adapter.out.persistence;

import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.auction.boundedContext.user.port.out.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * User Repository Implementation (Outbound Adapter)
 * 사용자 저장소 구현체
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    // TODO: JpaRepository 주입
    // private final UserJpaRepository jpaRepository;
    
    // TODO: Repository 메서드 구현
}

