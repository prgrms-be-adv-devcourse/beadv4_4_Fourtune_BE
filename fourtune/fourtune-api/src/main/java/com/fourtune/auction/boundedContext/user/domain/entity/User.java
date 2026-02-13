package com.fourtune.auction.boundedContext.user.domain.entity;

import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.common.global.common.BaseTimeEntity;
import com.fourtune.common.shared.user.dto.UserResponse;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "users")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column
    private String phoneNumber;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    private LocalDateTime deletedAt;

    @Version
    private Long version;

    private String provider;
    private String providerId;

    @Builder.Default
    private Long penaltyScore = 0L;

    public void imposePenalty(){
        if(this.penaltyScore == null){
            this.penaltyScore = 0L;
        }

        this.penaltyScore -= 10;
    }

    public void bannedUser(){
        this.status = Status.INACTIVE;
    }

    public void updateProfile(String newNickname, String newPhoneNumber) {
        this.nickname = newNickname;
        this.phoneNumber = newPhoneNumber;
    }

    public void changePassword(String newEncodedPassword) {
        this.password = newEncodedPassword;
    }

    public void changeStatus(Status status){
        this.status = status;
    }

    public void updateNickname(String nickname){
        this.nickname = nickname;
    }

    public void withdraw() {
        this.status = Status.SUSPENDED;
        this.deletedAt = LocalDateTime.now();
    }

    public boolean isAvailableUser(){
        if(this.status == Status.ACTIVE) return true;

        return false;
    }

    public User update(String nickname){
        this.nickname = nickname;

        return this;
    }

    public void updateOauth(String provider, String providerId){
        this.provider = provider;
        this.providerId = providerId;
    }

    public UserResponse toDto(){
        return new UserResponse(
                this.id,
                this.getCreatedAt(),
                this.getUpdatedAt(),
                this.email,
                this.nickname,
                this.status.parseToString(),
                this.role.name()
        );
    }

}

