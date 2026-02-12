package com.fourtune.auction.boundedContext.auth.adapter.in;

import com.fourtune.auction.boundedContext.auth.application.service.AuthService;
import com.fourtune.common.shared.auth.dto.ReissueRequest;
import com.fourtune.common.shared.auth.dto.TokenResponse;
import com.fourtune.common.shared.auth.dto.UserContext;
import com.fourtune.common.shared.user.dto.UserLoginRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid UserLoginRequest request) {
        TokenResponse token = authService.login(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/reissue")
    public ResponseEntity<TokenResponse> reissue(@RequestBody ReissueRequest request){
        TokenResponse tokenResponse = authService.reissue(request.refreshToken());

        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@AuthenticationPrincipal UserContext userContext) {
        authService.logout(userContext.id());

        return ResponseEntity.ok().build();
    }

}
