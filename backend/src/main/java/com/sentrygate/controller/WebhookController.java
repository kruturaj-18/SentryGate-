package com.sentrygate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrygate.entity.Issue;
import com.sentrygate.entity.PullRequest;
import com.sentrygate.entity.Repository;
import com.sentrygate.entity.WebhookEvent;
import com.sentrygate.repository.IssueRepository;
import com.sentrygate.repository.PullRequestRepository;
import com.sentrygate.repository.RepositoryRepository;
import com.sentrygate.repository.WebhookEventRepository;
import com.sentrygate.service.IssueService;
import com.sentrygate.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookEventRepository webhookEventRepository;
    private final RepositoryRepository repositoryRepository;
    private final PullRequestRepository pullRequestRepository;
    private final IssueRepository issueRepository;
    private final ValidationService validationService;
    private final IssueService issueService;
    private final ObjectMapper objectMapper;

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestHeader(value = "X-GitHub-Event", required = false) String eventType,
            @RequestHeader(value = "X-GitHub-Delivery", required = false) String deliveryId,
            @RequestBody String rawPayload) {

        log.info("Received GitHub Webhook event: {} [Delivery ID: {}]", eventType, deliveryId);

        WebhookEvent webhookEvent = WebhookEvent.builder()
                .eventType(eventType != null ? eventType : "unknown")
                .deliveryId(deliveryId)
                .payload(rawPayload)
                .processed(false)
                .receivedAt(OffsetDateTime.now())
                .build();

        webhookEventRepository.save(webhookEvent);

        try {
            JsonNode root = objectMapper.readTree(rawPayload);

            if ("pull_request".equalsIgnoreCase(eventType)) {
                handlePullRequestEvent(root);
            } else if ("issues".equalsIgnoreCase(eventType)) {
                handleIssueEvent(root);
            }

            webhookEvent.setProcessed(true);
            webhookEventRepository.save(webhookEvent);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing GitHub webhook event {}: {}", eventType, e.getMessage(), e);
            webhookEvent.setErrorMessage(e.getMessage());
            webhookEventRepository.save(webhookEvent);
            return ResponseEntity.ok("Webhook received with warning: " + e.getMessage());
        }
    }

    private void handlePullRequestEvent(JsonNode root) {
        String action = root.has("action") ? root.get("action").asText() : "";
        JsonNode prNode = root.get("pull_request");
        JsonNode repoNode = root.get("repository");

        if (prNode == null || repoNode == null) return;

        Long githubRepoId = repoNode.get("id").asLong();
        Optional<Repository> repoOpt = repositoryRepository.findByGithubRepoId(githubRepoId);
        if (repoOpt.isEmpty()) {
            log.warn("Repository with GitHub ID {} not found in database for PR event", githubRepoId);
            return;
        }

        Repository repo = repoOpt.get();
        Long githubPrId = prNode.get("id").asLong();
        int prNumber = prNode.get("number").asInt();

        PullRequest pr = pullRequestRepository.findByRepositoryIdAndPrNumber(repo.getId(), prNumber)
                .orElseGet(() -> PullRequest.builder()
                        .repository(repo)
                        .githubPrId(githubPrId)
                        .prNumber(prNumber)
                        .build());

        pr.setTitle(prNode.get("title").asText());
        pr.setBody(prNode.has("body") && !prNode.get("body").isNull() ? prNode.get("body").asText() : "");
        pr.setState(prNode.get("state").asText());
        pr.setAuthorUsername(prNode.get("user").get("login").asText());
        pr.setAuthorAvatarUrl(prNode.get("user").get("avatar_url").asText());
        pr.setSourceBranch(prNode.get("head").get("ref").asText());
        pr.setTargetBranch(prNode.get("base").get("ref").asText());
        pr.setHeadSha(prNode.get("head").get("sha").asText());
        pr.setAdditions(prNode.has("additions") ? prNode.get("additions").asInt() : 0);
        pr.setDeletions(prNode.has("deletions") ? prNode.get("deletions").asInt() : 0);
        pr.setChangedFilesCount(prNode.has("changed_files") ? prNode.get("changed_files").asInt() : 0);

        PullRequest savedPr = pullRequestRepository.save(pr);

        if ("opened".equalsIgnoreCase(action) || "synchronize".equalsIgnoreCase(action) || "reopened".equalsIgnoreCase(action)) {
            validationService.executeValidation(savedPr);
        }
    }

    private void handleIssueEvent(JsonNode root) {
        String action = root.has("action") ? root.get("action").asText() : "";
        JsonNode issueNode = root.get("issue");
        JsonNode repoNode = root.get("repository");

        if (issueNode == null || repoNode == null) return;

        Long githubRepoId = repoNode.get("id").asLong();
        Optional<Repository> repoOpt = repositoryRepository.findByGithubRepoId(githubRepoId);
        if (repoOpt.isEmpty()) return;

        Repository repo = repoOpt.get();
        Long githubIssueId = issueNode.get("id").asLong();
        int issueNumber = issueNode.get("number").asInt();

        Issue issue = issueRepository.findByRepositoryIdAndIssueNumber(repo.getId(), issueNumber)
                .orElseGet(() -> Issue.builder()
                        .repository(repo)
                        .githubIssueId(githubIssueId)
                        .issueNumber(issueNumber)
                        .build());

        issue.setTitle(issueNode.get("title").asText());
        issue.setBody(issueNode.has("body") && !issueNode.get("body").isNull() ? issueNode.get("body").asText() : "");
        issue.setState(issueNode.get("state").asText());
        issue.setAuthorUsername(issueNode.get("user").get("login").asText());
        issue.setAuthorAvatarUrl(issueNode.get("user").get("avatar_url").asText());

        if ("opened".equalsIgnoreCase(action) || "edited".equalsIgnoreCase(action)) {
            issueService.saveIssueAndDetectDuplicates(issue);
        } else {
            issueRepository.save(issue);
        }
    }
}
