package com.fourtune.auction.boundedContext.watchList.domain;

import com.fourtune.auction.shared.user.domain.ReplicaUser;
import com.fourtune.auction.shared.watchList.dto.WatchListUserDto;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "watch_list_user")
@Getter
@NoArgsConstructor
public class WatchListUser extends ReplicaUser {

    public WatchListUser(
            Long id,
            String email,
            String nickname,
            String password,
            String phoneNumber,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            LocalDateTime deletedAt
    ){
        super(id, email, nickname, password, phoneNumber, createdAt, updatedAt, deletedAt);
    }

    public WatchListUserDto toDto(){
        return new WatchListUserDto(
                getId(),
                getCreatedAt(),
                getUpdatedAt(),
                getNickname()
        );
    }

}
