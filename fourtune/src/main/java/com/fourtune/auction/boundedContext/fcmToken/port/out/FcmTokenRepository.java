package com.fourtune.auction.boundedContext.fcmToken.port.out;

import com.fourtune.auction.boundedContext.fcmToken.domain.FcmToken;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
    boolean existsByToken(String token);

    Optional<FcmToken> findByToken(String token);

    @Query("select f.token from FcmToken f where f.userId = :userId")
    List<String> findAllTokensByUserId(@Param("userId") Long userId);
}
