package com.fourtune.auction.adapter.out.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * fourtune-api User API 호출용 Feign 클라이언트.
 * GET /api/users/nicknames?ids=1&ids=2
 */
@FeignClient(
    name = "user-service",
    url = "${api.user.base-url}",
    configuration = com.fourtune.auction.adapter.out.api.FeignConfig.class
)
public interface UserClient {

    @GetMapping("/api/users/nicknames")
    Map<String, String> getNicknamesByIds(@RequestParam("ids") List<Long> ids);
}
