package com.sentrygate.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrygate.config.SentryGateProperties;
import com.sentrygate.entity.Issue;
import com.sentrygate.entity.PullRequest;
import com.sentrygate.entity.Repository;
import com.sentrygate.entity.WebhookEvent;
import com.sentrygate.github.GitHubClient;
import com.sentrygate.repository.IssueRepository;
import com.sentrygate.repository.PullRequestRepository;
import com.sentrygate.repository.RepositoryRepository;
import com.sentrygate.repository.WebhookEventRepository;
import com.sentrygate.service.IssueService;
import com.sentrygate.service.ValidationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
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
    private final GitHubClient gitHubClient;
    private final ObjectMapper objectMapper;
    private final SentryGateProperties properties;

    @PostMapping("/github")
    public ResponseEntity<String> handleGitHubWebhook(
            @RequestHeader(value = "X-GitHub-Event",        required = false) String eventType,
            @RequestHeader(value = "X-GitHub-Delivery",     required = false) String deliveryId,
            // Issue #3: read the HMAC-SHA256 signature header
            @RequestHeader(value = "X-Hub-Signature-256",   required = false) String signatureHeader,
            @RequestBody String rawPayload) {

        log.info("Received GitHub Webhook event: {} [Delivery ID: {}]", eventType, deliveryId);

        // ---------------------------------------------------------------- //
        // Issue #3 fix: HMAC-SHA256 signature verification                  //
        // ---------------------------------------------------------------- //
        String webhookSecret = properties.getGithub().getWebhookSecret();
        if (webhookSecret != null && !webhookSecret.isBlank()) {
            if (signatureHeader == null || signatureHeader.isBlank()) {
                log.warn("Rejecting webhook – missing X-Hub-Signature-256 header");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Missing X-Hub-Signature-256");
            }
            if (!isValidSignature(rawPayload, signatureHeader, webhookSecret)) {
                log.warn("Rejecting webhook – HMAC-SHA256 signature mismatch for delivery {}", deliveryId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid webhook signature");
            }
        }

        Map<String, Object> payloadMap = null;
        try {
            payloadMap = objectMapper.readValue(rawPayload, new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
        } catch (Exception ignored) {}

        WebhookEvent webhookEvent = WebhookEvent.builder()
                .eventType(eventType != null ? eventType : "unknown")
                .deliveryId(deliveryId)
                .payload(payloadMap)
                .processingStatus("PENDING")
                .build();

        webhookEventRepository.save(webhookEvent);

        try {
            JsonNode root = objectMapper.readTree(rawPayload);

            if ("pull_request".equalsIgnoreCase(eventType)) {
                handlePullRequestEvent(root);
            } else if ("issues".equalsIgnoreCase(eventType)) {
                handleIssueEvent(root);
            }

            webhookEvent.setProcessingStatus("SUCCESS");
            webhookEvent.setProcessedAt(OffsetDateTime.now());
            webhookEventRepository.save(webhookEvent);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing GitHub webhook event {}: {}", eventType, e.getMessage(), e);
            webhookEvent.setProcessingStatus("FAILED");
            webhookEvent.setErrorMessage(e.getMessage());
            webhookEvent.setProcessedAt(OffsetDateTime.now());
            webhookEventRepository.save(webhookEvent);
            return ResponseEntity.ok("Webhook received with warning: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------- //
    // Issue #3 helper: constant-time HMAC-SHA256 comparison                 //
    // -------------------------------------------------------------------- //
    private boolean isValidSignature(String payload, String signatureHeader, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(computed);

            // Constant-time compare to prevent timing attacks
            if (signatureHeader.length() != expected.length()) return false;
            int diff = 0;
            for (int i = 0; i < signatureHeader.length(); i++) {
                diff |= signatureHeader.charAt(i) ^ expected.charAt(i);
            }
            return diff == 0;
        } catch (Exception e) {
            log.error("HMAC-SHA256 verification error: {}", e.getMessage());
            return false;
        }
    }

    private void handlePullRequestEvent(JsonNode root) {
        String action  = root.has("action") ? root.get("action").asText() : "";
        JsonNode prNode   = root.get("pull_request");
        JsonNode repoNode = root.get("repository");

        if (prNode == null || repoNode == null) return;

        Long githubRepoId = repoNode.get("id").asLong();
        Optional<Repository> repoOpt = repositoryRepository.findByGithubRepoId(githubRepoId);
        if (repoOpt.isEmpty()) {
            log.warn("Repository with GitHub ID {} not found in database for PR event", githubRepoId);
            return;
        }

        Repository repo  = repoOpt.get();
        Long githubPrId  = prNode.get("id").asLong();
        int  prNumber    = prNode.get("number").asInt();

        PullRequest pr = pullRequestRepository.findByRepositoryIdAndNumber(repo.getId(), prNumber)
                .orElseGet(() -> PullRequest.builder()
                        .repository(repo)
                        .githubPrId(githubPrId)
                        .number(prNumber)
                        .build());

        pr.setTitle(prNode.get("title").asText());
        pr.setBody(prNode.has("body") && !prNode.get("body").isNull() ? prNode.get("body").asText() : "");
        pr.setState(prNode.get("state").asText());
        pr.setAuthorLogin(prNode.get("user").get("login").asText());
        pr.setHeadBranch(prNode.get("head").get("ref").asText());
        pr.setBaseBranch(prNode.get("base").get("ref").asText());
        pr.setHeadSha(prNode.get("head").get("sha").asText());
        pr.setAdditions(prNode.has("additions")    ? prNode.get("additions").asInt()     : 0);
        pr.setDeletions(prNode.has("deletions")    ? prNode.get("deletions").asInt()     : 0);
        pr.setChangedFiles(prNode.has("changed_files") ? prNode.get("changed_files").asInt() : 0);

        PullRequest savedPr = pullRequestRepository.save(pr);

        if ("opened".equalsIgnoreCase(action)
                || "synchronize".equalsIgnoreCase(action)
                || "reopened".equalsIgnoreCase(action)) {

            // ------------------------------------------------------------ //
            // Issue #2 fix: fetch real changed file names from GitHub API   //
            // so file-based rules (RequiredFiles, TestPresence, etc.) get   //
            // actual data instead of Collections.emptyList().               //
            // ------------------------------------------------------------ //
            List<String> changedFileNames = fetchChangedFileNames(repo, savedPr);
            validationService.executeValidation(savedPr, changedFileNames);
        }
    }

    /**
     * Issue #2 fix: calls the GitHub Files API to retrieve filenames for this PR.
     * Falls back to an empty list if the repository has no access token or the
     * call fails, so validation still runs (with file-based rules reporting FAIL
     * instead of silently passing with empty data).
     */
    private List<String> fetchChangedFileNames(Repository repo, PullRequest pr) {
        try {
            String accessToken = (repo.getConnectedUser() != null)
                    ? repo.getConnectedUser().getGithubToken()
                    : null;

            if (accessToken == null || accessToken.isBlank()) {
                log.debug("No access token for repo {}; skipping file fetch for PR #{}",
                        repo.getFullName(), pr.getNumber());
                return Collections.emptyList();
            }

            JsonNode filesNode = gitHubClient.getPullRequestFiles(
                    repo.getOwner(), repo.getName(), pr.getNumber(), accessToken);

            List<String> names = new ArrayList<>();
            if (filesNode != null && filesNode.isArray()) {
                filesNode.forEach(f -> {
                    if (f.has("filename")) {
                        names.add(f.get("filename").asText());
                    }
                });
            }
            log.info("Fetched {} changed files for PR #{} in {}", names.size(), pr.getNumber(), repo.getFullName());
            return names;

        } catch (Exception e) {
            log.warn("Could not fetch changed files for PR #{} in {}: {}",
                    pr.getNumber(), repo.getFullName(), e.getMessage());
            return Collections.emptyList();
        }
    }

    private void handleIssueEvent(JsonNode root) {
        String action     = root.has("action")    ? root.get("action").asText()    : "";
        JsonNode issueNode  = root.get("issue");
        JsonNode repoNode   = root.get("repository");

        if (issueNode == null || repoNode == null) return;

        Long githubRepoId = repoNode.get("id").asLong();
        Optional<Repository> repoOpt = repositoryRepository.findByGithubRepoId(githubRepoId);
        if (repoOpt.isEmpty()) return;

        Repository repo      = repoOpt.get();
        Long githubIssueId   = issueNode.get("id").asLong();
        int  issueNumber     = issueNode.get("number").asInt();

        Issue issue = issueRepository.findByRepositoryIdAndNumber(repo.getId(), issueNumber)
                .orElseGet(() -> Issue.builder()
                        .repository(repo)
                        .githubIssueId(githubIssueId)
                        .number(issueNumber)
                        .build());

        issue.setTitle(issueNode.get("title").asText());
        issue.setBody(issueNode.has("body") && !issueNode.get("body").isNull() ? issueNode.get("body").asText() : "");
        issue.setState(issueNode.get("state").asText());
        issue.setAuthorLogin(issueNode.get("user").get("login").asText());

        if ("opened".equalsIgnoreCase(action) || "edited".equalsIgnoreCase(action)) {
            issueService.saveIssueAndDetectDuplicates(issue);
        } else {
            issueRepository.save(issue);
        }
    }
}
