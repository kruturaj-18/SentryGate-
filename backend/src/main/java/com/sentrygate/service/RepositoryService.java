package com.sentrygate.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.sentrygate.dto.RepositoryDto;
import com.sentrygate.dto.RepositoryRuleDto;
import com.sentrygate.entity.Repository;
import com.sentrygate.entity.RepositoryRule;
import com.sentrygate.entity.User;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.github.GitHubClient;
import com.sentrygate.repository.RepositoryRepository;
import com.sentrygate.repository.RepositoryRuleRepository;
import com.sentrygate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;
    private final RepositoryRuleRepository repositoryRuleRepository;
    private final UserRepository userRepository;
    private final GitHubClient gitHubClient;

    @Transactional(readOnly = true)
    public List<RepositoryDto> getAllRepositories() {
        return repositoryRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RepositoryDto> getUserRepositories(UUID userId) {
        return repositoryRepository.findByOwnerId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RepositoryDto getRepositoryById(UUID repoId) {
        Repository repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));
        return mapToDto(repo);
    }

    @Transactional
    public List<RepositoryDto> syncUserRepositories(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getGithubAccessToken() == null) {
            log.warn("User {} has no GitHub access token, skipping remote sync", user.getUsername());
            return getUserRepositories(userId);
        }

        JsonNode reposNode = gitHubClient.getUserRepositories(user.getGithubAccessToken());
        List<RepositoryDto> synced = new ArrayList<>();

        if (reposNode != null && reposNode.isArray()) {
            for (JsonNode repoNode : reposNode) {
                Long githubRepoId = repoNode.get("id").asLong();
                String name = repoNode.get("name").asText();
                String fullName = repoNode.get("full_name").asText();
                String owner = repoNode.get("owner").get("login").asText();
                String defaultBranch = repoNode.has("default_branch") ? repoNode.get("default_branch").asText() : "main";

                Repository repo = repositoryRepository.findByGithubRepoId(githubRepoId)
                        .orElseGet(() -> Repository.builder()
                                .githubRepoId(githubRepoId)
                                .name(name)
                                .fullName(fullName)
                                .owner(owner)
                                .defaultBranch(defaultBranch)
                                .ownerUser(user)
                                .isActive(true)
                                .qualityGateEnabled(true)
                                .duplicateDetectionEnabled(true)
                                .trustScoringEnabled(true)
                                .build());

                repo.setName(name);
                repo.setFullName(fullName);
                repo.setOwner(owner);
                repo.setDefaultBranch(defaultBranch);

                Repository saved = repositoryRepository.save(repo);
                synced.add(mapToDto(saved));
            }
        }

        return synced;
    }

    @Transactional
    public RepositoryDto toggleQualityGate(UUID repoId, boolean enabled) {
        Repository repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));
        repo.setQualityGateEnabled(enabled);
        return mapToDto(repositoryRepository.save(repo));
    }

    @Transactional(readOnly = true)
    public List<RepositoryRuleDto> getRepositoryRules(UUID repoId) {
        List<RepositoryRule> rules = repositoryRuleRepository.findByRepositoryId(repoId);
        return rules.stream()
                .map(r -> RepositoryRuleDto.builder()
                        .id(r.getId())
                        .repositoryId(r.getRepository().getId())
                        .ruleCode(r.getRuleCode())
                        .enabled(r.getIsEnabled())
                        .config(r.getConfigJson())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public RepositoryRuleDto updateRepositoryRule(UUID repoId, String ruleCode, boolean enabled, Map<String, Object> config) {
        Repository repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        RepositoryRule rule = repositoryRuleRepository.findByRepositoryIdAndRuleCode(repoId, ruleCode)
                .orElseGet(() -> RepositoryRule.builder()
                        .repository(repo)
                        .ruleCode(ruleCode)
                        .build());

        rule.setIsEnabled(enabled);
        if (config != null) {
            rule.setConfigJson(config.toString());
        }
        RepositoryRule saved = repositoryRuleRepository.save(rule);

        return RepositoryRuleDto.builder()
                .id(saved.getId())
                .repositoryId(repo.getId())
                .ruleCode(saved.getRuleCode())
                .enabled(saved.getIsEnabled())
                .config(saved.getConfigJson())
                .build();
    }

    private RepositoryDto mapToDto(Repository repo) {
        return RepositoryDto.builder()
                .id(repo.getId())
                .githubRepoId(repo.getGithubRepoId())
                .name(repo.getName())
                .fullName(repo.getFullName())
                .owner(repo.getOwner())
                .defaultBranch(repo.getDefaultBranch())
                .isActive(repo.getIsActive())
                .qualityGateEnabled(repo.getQualityGateEnabled())
                .duplicateDetectionEnabled(repo.getDuplicateDetectionEnabled())
                .trustScoringEnabled(repo.getTrustScoringEnabled())
                .createdAt(repo.getCreatedAt())
                .updatedAt(repo.getUpdatedAt())
                .build();
    }
}
