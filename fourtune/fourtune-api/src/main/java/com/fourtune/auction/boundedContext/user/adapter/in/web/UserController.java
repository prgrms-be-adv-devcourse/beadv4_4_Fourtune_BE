package com.fourtune.auction.boundedContext.user.adapter.in.web;

import com.fourtune.auction.boundedContext.user.application.service.UserFacade;
import com.fourtune.common.shared.auth.dto.UserContext;
import com.fourtune.common.shared.user.dto.*;
import com.fourtune.common.shared.user.dto.UserPasswordChangeRequest;
import com.fourtune.common.shared.user.dto.UserSignUpRequest;
import com.fourtune.common.shared.user.dto.UserUpdateRequest;
import com.fourtune.common.shared.user.dto.UserWithdrawRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserFacade userFacade;

    @PostMapping("/signup")
    public ResponseEntity<Void> signup(@RequestBody @Valid UserSignUpRequest request) {
        userFacade.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    /*
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest request) {
        UserLoginResponse response = userFacade.login(request);
        return ResponseEntity.ok(response);
    }
    */
    @PatchMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @AuthenticationPrincipal UserContext user,
            @RequestBody @Valid UserUpdateRequest request
    ) {
        Long userId = user.id();
        userFacade.updateProfile(userId, request);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserContext user,
            @RequestBody @Valid UserPasswordChangeRequest request
    ) {
        Long userId = user.id();
        userFacade.changePassword(userId, request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @AuthenticationPrincipal UserContext user,
            @RequestBody @Valid UserWithdrawRequest request
    ) {
        Long userId = user.id();
        userFacade.withdraw(userId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * ID로 유저 정보 조회 (id, email, nickname, status 등)
     */
    @GetMapping("/{id}")
    public ResponseEntity<com.fourtune.common.shared.user.dto.UserResponse> getUser(@PathVariable Long id) {
        com.fourtune.common.shared.user.dto.UserResponse response = userFacade.getUserById(id);
        return ResponseEntity.ok(response);
    }

}
