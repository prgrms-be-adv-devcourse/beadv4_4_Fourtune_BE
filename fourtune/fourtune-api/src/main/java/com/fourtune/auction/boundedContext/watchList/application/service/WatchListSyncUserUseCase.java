package com.fourtune.auction.boundedContext.watchList.application.service;

import com.fourtune.auction.boundedContext.watchList.domain.WatchListUser;
import com.fourtune.common.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WatchListSyncUserUseCase {

    private final WatchListSupport watchListSupport;

    public void syncUser(UserResponse userResponse){
        log.info("유저 동기화 시작 - UserId: {}", userResponse.id());

        watchListSupport.findOptionalByUserId(userResponse.id())
                .ifPresentOrElse(
                        existingUser -> existingUser.syncProfile(userResponse.nickname(), userResponse.email(), userResponse.status()),
                        () -> {
                            WatchListUser newUser = WatchListUser.builder()
                                    .id(userResponse.id())
                                    .createdAt(userResponse.createdAt())
                                    .updatedAt(userResponse.updatedAt())
                                    .email(userResponse.email())
                                    .nickname(userResponse.nickname())
                                    .status(userResponse.status())
                                    .deletedAt(null)
                                    .phoneNumber("")
                                    .password("")
                                    .build();

                            watchListSupport.saveWatchListUser(newUser);
                        }
                );
    }

}
