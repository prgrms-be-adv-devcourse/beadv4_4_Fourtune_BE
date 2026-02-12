package com.fourtune.auction.boundedContext.user.mapper;

import com.fourtune.auction.boundedContext.user.domain.constant.Role;
import com.fourtune.auction.boundedContext.user.domain.constant.Status;
import com.fourtune.auction.boundedContext.user.domain.entity.User;
import com.fourtune.common.shared.user.dto.UserResponse;
import com.fourtune.common.shared.user.dto.UserSignUpRequest;
import org.springframework.stereotype.Component;

public class UserMapper {

    public static UserResponse toDto(User user){
        return UserResponse.builder()
                .id(user.getId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .role(user.getRole().getKey())
                .status(user.getStatus().parseToString())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .build();
    }

    public static User toEntity(UserSignUpRequest userSignUpRequest, String encodedPassword) {
        return User.builder()
                .email(userSignUpRequest.email())
                .nickname(userSignUpRequest.nickname())
                .password(encodedPassword)
                .phoneNumber(userSignUpRequest.phoneNumber())
                .role(Role.USER)
                .status(Status.ACTIVE)
                .build();
    }

}
