package com.fourtune.auction.boundedContext.watchList.domain;

import com.fourtune.common.shared.user.domain.ReplicaUser;
import com.fourtune.common.shared.watchList.dto.WatchListUserDto;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "watch_list_user")
@Getter
@NoArgsConstructor
public class WatchListUser extends ReplicaUser {

    @Builder
    public WatchListUser(
            Long id,
            String email,
            String nickname,
            String password,
            String phoneNumber,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt,
            String status
    ){
        super(id, email, nickname, password, phoneNumber, createdAt, updatedAt, deletedAt, status);
    }

    public WatchListUserDto toDto(){
        return new WatchListUserDto(
                getId(),
                getCreatedAt(),
                getUpdatedAt(),
                getNickname()
        );
    }

    public void syncProfile(String nickname, String email, String status) {
        super.updateInfo(nickname, email, status);
    }

}
