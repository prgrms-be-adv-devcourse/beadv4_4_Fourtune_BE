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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

    /**
     * ID 목록으로 닉네임 맵 조회 (경매 등 다른 서비스에서 Feign 호출용).
     * GET /api/users/nicknames?ids=1&ids=2&ids=3
     */
    @GetMapping("/nicknames")
    public ResponseEntity<Map<String, String>> getNicknamesByIds(@RequestParam("ids") List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(Map.of());
        }
        Map<Long, String> map = userFacade.getNicknamesByIds(Set.copyOf(ids));
        Map<String, String> response = map.entrySet().stream()
                .collect(Collectors.toMap(e -> String.valueOf(e.getKey()), Map.Entry::getValue));
        return ResponseEntity.ok(response);
    }

}
