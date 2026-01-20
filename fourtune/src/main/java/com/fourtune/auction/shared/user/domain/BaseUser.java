package com.fourtune.auction.shared.user.domain;


import com.fourtune.auction.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static lombok.AccessLevel.PROTECTED;

@MappedSuperclass
@Getter
@NoArgsConstructor
public abstract class BaseUser extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, length = 20)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String status;

    public BaseUser(String email, String nickname, String password, String phoneNumber, String status) {
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.status = status;
    }

    public void updateInfo(String nickname, String email, String status) {
        this.nickname = nickname;
        this.email = email;
        this.status = status;
    }

}
