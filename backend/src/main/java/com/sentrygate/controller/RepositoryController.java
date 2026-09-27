package com.sentrygate.controller;

import com.sentrygate.dto.RepositoryDto;
import com.sentrygate.dto.RepositoryRuleDto;
import com.sentrygate.security.UserPrincipal;
import com.sentrygate.service.RepositoryService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/repositories")
@RequiredArgsConstructor
public class RepositoryController {

    private final RepositoryService repositoryService;

    @GetMapping
    public ResponseEntity<List<RepositoryDto>> getRepositories(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser != null) {
            return ResponseEntity.ok(repositoryService.getUserRepositories(currentUser.getId()));
        }
        return ResponseEntity.ok(repositoryService.getAllRepositories());
    }

    @PostMapping("/sync")
    public ResponseEntity<List<RepositoryDto>> syncRepositories(@AuthenticationPrincipal UserPrincipal currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(repositoryService.syncUserRepositories(currentUser.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RepositoryDto> getRepositoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(repositoryService.getRepositoryById(id));
    }

    @PutMapping("/{id}/toggle-gate")
    public ResponseEntity<RepositoryDto> toggleQualityGate(
            @PathVariable UUID id,
            @RequestParam boolean enabled) {
        return ResponseEntity.ok(repositoryService.toggleQualityGate(id, enabled));
    }

    @GetMapping("/{id}/rules")
    public ResponseEntity<List<RepositoryRuleDto>> getRepositoryRules(@PathVariable UUID id) {
        return ResponseEntity.ok(repositoryService.getRepositoryRules(id));
    }

    @Data
    public static class RuleUpdateRequest {
        private String ruleCode;
        private boolean enabled;
        private Map<String, Object> config;
    }

    @PutMapping("/{id}/rules")
    public ResponseEntity<RepositoryRuleDto> updateRepositoryRule(
            @PathVariable UUID id,
            @RequestBody RuleUpdateRequest request) {
        return ResponseEntity.ok(repositoryService.updateRepositoryRule(id, request.getRuleCode(), request.isEnabled(), request.getConfig()));
    }
}
