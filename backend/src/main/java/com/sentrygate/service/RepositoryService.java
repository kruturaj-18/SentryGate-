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
    public List<RepositoryDto> getUserRepositories(Long userId) {
        return repositoryRepository.findByConnectedUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RepositoryDto getRepositoryById(Long repoId) {
        Repository repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));
        return mapToDto(repo);
    }

    @Transactional
    public RepositoryDto toggleQualityGate(Long repoId, boolean enabled) {
        Repository repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));
        repo.setMonitoringStatus(enabled ? "ACTIVE" : "INACTIVE");
        return mapToDto(repositoryRepository.save(repo));
    }

    @Transactional
    public List<RepositoryDto> syncUserRepositories(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getGithubToken() == null) {
            log.warn("User {} has no GitHub access token, skipping remote sync", user.getLogin());
            return getUserRepositories(userId);
        }

        JsonNode reposNode = gitHubClient.getUserRepositories(user.getGithubToken());
        List<RepositoryDto> synced = new ArrayList<>();

        if (reposNode != null && reposNode.isArray()) {
            for (JsonNode repoNode : reposNode) {
                Long githubRepoId  = repoNode.get("id").asLong();
                String name        = repoNode.get("name").asText();
                String fullName    = repoNode.get("full_name").asText();
                String owner       = repoNode.get("owner").get("login").asText();
                String defaultBranch = repoNode.has("default_branch")
                        ? repoNode.get("default_branch").asText() : "main";
                String htmlUrl     = repoNode.has("html_url") ? repoNode.get("html_url").asText() : null;
                String description = repoNode.has("description") && !repoNode.get("description").isNull()
                        ? repoNode.get("description").asText() : null;
                String language    = repoNode.has("language") && !repoNode.get("language").isNull()
                        ? repoNode.get("language").asText() : null;
                boolean privateRepo = repoNode.has("private") && repoNode.get("private").asBoolean();

                Repository repo = repositoryRepository.findByGithubRepoId(githubRepoId)
                        .orElseGet(() -> Repository.builder()
                                .githubRepoId(githubRepoId)
                                .connectedUser(user)
                                .build());

                repo.setName(name);
                repo.setFullName(fullName);
                repo.setOwner(owner);
                repo.setDefaultBranch(defaultBranch);
                repo.setHtmlUrl(htmlUrl);
                repo.setDescription(description);
                repo.setLanguage(language);
                repo.setPrivateRepo(privateRepo);

                Repository saved = repositoryRepository.save(repo);
                synced.add(mapToDto(saved));
            }
        }
        return synced;
    }

    @Transactional(readOnly = true)
    public List<RepositoryRuleDto> getRepositoryRules(Long repoId) {
        List<RepositoryRule> rules = repositoryRuleRepository.findByRepositoryId(repoId);
        return rules.stream()
                .map(r -> RepositoryRuleDto.builder()
                        .id(r.getId())
                        .repositoryId(r.getRepository().getId())
                        .ruleCode(r.getRuleCode())
                        .ruleName(r.getRuleName())
                        .enabled(r.getEnabled())
                        .severity(r.getSeverity())
                        .configuration(r.getConfiguration())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public RepositoryRuleDto updateRepositoryRule(Long repoId, String ruleCode,
                                                  boolean enabled, Map<String, Object> config) {
        Repository repo = repositoryRepository.findById(repoId)
                .orElseThrow(() -> new ResourceNotFoundException("Repository", "id", repoId));

        RepositoryRule rule = repositoryRuleRepository.findByRepositoryIdAndRuleCode(repoId, ruleCode)
                .orElseGet(() -> RepositoryRule.builder()
                        .repository(repo)
                        .ruleCode(ruleCode)
                        .build());

        rule.setEnabled(enabled);
        if (config != null) {
            rule.setConfiguration(config);
        }
        RepositoryRule saved = repositoryRuleRepository.save(rule);

        return RepositoryRuleDto.builder()
                .id(saved.getId())
                .repositoryId(repo.getId())
                .ruleCode(saved.getRuleCode())
                .ruleName(saved.getRuleName())
                .enabled(saved.getEnabled())
                .severity(saved.getSeverity())
                .configuration(saved.getConfiguration())
                .build();
    }

    private RepositoryDto mapToDto(Repository repo) {
        return RepositoryDto.builder()
                .id(repo.getId())
                .githubRepoId(repo.getGithubRepoId())
                .name(repo.getName())
                .fullName(repo.getFullName())
                .owner(repo.getOwner())
                .htmlUrl(repo.getHtmlUrl())
                .description(repo.getDescription())
                .defaultBranch(repo.getDefaultBranch())
                .privateRepo(repo.getPrivateRepo())
                .language(repo.getLanguage())
                .monitoringStatus(repo.getMonitoringStatus())
                .openPrCount(repo.getOpenPrCount())
                .openIssueCount(repo.getOpenIssueCount())
                .lastSyncedAt(repo.getLastSyncedAt())
                .createdAt(repo.getCreatedAt())
                .build();
    }
}
