package com.fourtune.auction.boundedContext.user.domain.entity;

import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.global.common.BaseIdAndTime;
import com.fourtune.auction.global.common.BaseTimeEntity;
import com.fourtune.auction.shared.user.dto.UserResponse;
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
public class User extends BaseTimeEntity {

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

    @Column(length = 1000)
    private String refreshToken;

    @Version
    private Long version;

    private String provider;
    private String providerId;

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

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean isAvailableUser(){
        if(this.status == Status.ACTIVE) return true;

        return false;
    }

    public User update(String name) {
        this.nickname = name;
        return this;
    }

    public UserResponse toDto(){
        return new UserResponse(
                this.id,
                this.getCreatedAt(),
                this.getUpdatedAt(),
                this.email,
                this.nickname,
                this.status.parseToString()
        );
    }

}

