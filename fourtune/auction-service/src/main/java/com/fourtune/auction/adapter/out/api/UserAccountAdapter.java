package com.fourtune.auction.adapter.out.api;

import com.fourtune.auction.port.out.UserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * UserPort 구현체. UserClient(Feign)를 통해 fourtune-api 닉네임 API 호출.
 */
@Component
@RequiredArgsConstructor
public class UserAccountAdapter implements UserPort {

    private final UserClient userClient;

    @Override
    public Map<Long, String> getNicknamesByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> response = userClient.getNicknamesByIds(ids.stream().toList());
        return response.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getKey()),
                        Map.Entry::getValue,
                        (a, b) -> a
                ));
    }
}
