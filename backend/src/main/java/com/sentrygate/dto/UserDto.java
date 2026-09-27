package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserDto {
    private Long id;
    private Long githubId;
    private String login;
    private String name;
    private String email;
    private String avatarUrl;
    private List<String> roles;
    private OffsetDateTime lastLoginAt;
    private OffsetDateTime createdAt;
}
