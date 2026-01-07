package com.fourtune.auction.boundedContext.user.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * User Controller (Inbound Adapter)
 * 사용자 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    // TODO: UseCase 주입
    // private final UserCommandUseCase userCommandUseCase;
    
    // TODO: API 엔드포인트 구현
    // @PostMapping
    // public ApiResponse<UserResponse> createUser(@RequestBody @Valid UserRequest request)
}

