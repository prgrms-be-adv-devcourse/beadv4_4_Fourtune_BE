package com.fourtune.auction.boundedContext.auth.port.out;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

	private static final String KEY_PREFIX = "refresh_token:";
	private static final long TTL_DAYS = 14; // 2주

	private final RedisTemplate<String, Object> redisTemplate;

	public void save(Long userId, String refreshToken) {
		String key = KEY_PREFIX + userId;
		redisTemplate.opsForValue().set(key, refreshToken, TTL_DAYS, TimeUnit.DAYS);
	}

	public Optional<String> findByUserId(Long userId) {
		String key = KEY_PREFIX + userId;
		Object token = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(token).map(Object::toString);
	}

	public void deleteByUserId(Long userId) {
		String key = KEY_PREFIX + userId;
		redisTemplate.delete(key);
	}

	public boolean existsByUserId(Long userId) {
		String key = KEY_PREFIX + userId;
		return redisTemplate.hasKey(key);
	}
}
