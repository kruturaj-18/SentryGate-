package com.sentrygate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrygate.dto.UserDto;
import com.sentrygate.entity.Role;
import com.sentrygate.entity.User;
import com.sentrygate.exception.BadRequestException;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.github.GitHubClient;
import com.sentrygate.repository.RoleRepository;
import com.sentrygate.repository.UserRepository;
import com.sentrygate.security.JwtTokenProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtTokenProvider tokenProvider;
    private final GitHubClient gitHubClient;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AuthResponse {
        private String token;
        private UserDto user;
    }

    /**
     * Authenticate or register user using GitHub Access Token
     */
    @Transactional
    public AuthResponse authenticateWithGitHub(String githubAccessToken) {
        log.info("Authenticating user with GitHub access token");
        JsonNode profile = gitHubClient.getUserProfile(githubAccessToken);

        if (profile == null || !profile.has("id")) {
            throw new BadRequestException("Invalid GitHub access token or profile data");
        }

        Long githubId = profile.get("id").asLong();
        String username = profile.get("login").asText();
        String email = profile.has("email") && !profile.get("email").isNull() ? profile.get("email").asText() : username + "@users.noreply.github.com";
        String avatarUrl = profile.has("avatar_url") ? profile.get("avatar_url").asText() : null;
        String fullName = profile.has("name") && !profile.get("name").isNull() ? profile.get("name").asText() : username;

        User user = userRepository.findByGithubId(githubId)
                .orElseGet(() -> {
                    Role userRole = roleRepository.findByName("ROLE_USER")
                            .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").description("Standard User").build()));
                    
                    Set<Role> roles = new HashSet<>();
                    roles.add(userRole);

                    return User.builder()
                            .githubId(githubId)
                            .username(username)
                            .email(email)
                            .avatarUrl(avatarUrl)
                            .fullName(fullName)
                            .roles(roles)
                            .isActive(true)
                            .build();
                });

        user.setGithubAccessToken(githubAccessToken);
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);

        String jwt = tokenProvider.generateToken(user.getId(), user.getUsername());

        return AuthResponse.builder()
                .token(jwt)
                .user(mapToUserDto(user))
                .build();
    }

    public UserDto getCurrentUserDto(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return mapToUserDto(user);
    }

    public UserDto mapToUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .githubId(user.getGithubId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .createdAt(user.getCreatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }
}
