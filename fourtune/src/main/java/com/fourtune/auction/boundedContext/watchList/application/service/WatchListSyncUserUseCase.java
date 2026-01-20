package com.fourtune.auction.boundedContext.watchList.application.service;

import com.fourtune.auction.boundedContext.watchList.domain.WatchListUser;
import com.fourtune.auction.shared.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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
                                    .build();

                            watchListSupport.saveWatchListUser(newUser);
                        }
                );
    }

}
