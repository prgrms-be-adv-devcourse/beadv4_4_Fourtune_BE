package com.fourtune.auction.port.out;

import java.util.Map;
import java.util.Set;

/**
 * 경매 도메인에서 사용자(닉네임 등) 정보를 조회하는 포트.
 * 구현체는 Feign 등으로 user-service(fourtune-api) API 호출.
 */
public interface UserPort {

    /**
     * ID 목록으로 닉네임 맵 조회 (응답 DTO용).
     */
    Map<Long, String> getNicknamesByIds(Set<Long> ids);
}
