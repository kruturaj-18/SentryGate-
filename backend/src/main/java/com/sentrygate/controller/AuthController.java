package com.sentrygate.controller;

import com.sentrygate.dto.UserDto;
import com.sentrygate.security.UserPrincipal;
import com.sentrygate.service.AuthService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Data
    public static class GitHubLoginRequest {
        private String code;
        private String accessToken;
    }

    @PostMapping("/github")
    public ResponseEntity<AuthService.AuthResponse> githubLogin(@RequestBody GitHubLoginRequest request) {
        // If access token is passed directly or exchanged from code
        String token = request.getAccessToken() != null ? request.getAccessToken() : request.getCode();
        AuthService.AuthResponse response = authService.authenticateWithGitHub(token);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        UserDto user = authService.getCurrentUserDto(currentUser.getId());
        return ResponseEntity.ok(user);
    }
}
