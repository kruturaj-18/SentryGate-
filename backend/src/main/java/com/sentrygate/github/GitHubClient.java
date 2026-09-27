package com.sentrygate.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrygate.config.SentryGateProperties;
import com.sentrygate.exception.GitHubApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GitHubClient {

    private final RestTemplate restTemplate;
    private final SentryGateProperties properties;
    private final ObjectMapper objectMapper;

    /**
     * Fetch user profile from GitHub using user OAuth token
     */
    public JsonNode getUserProfile(String accessToken) {
        String url = properties.getGithub().getApiUrl() + "/user";
        return makeGetRequest(url, accessToken);
    }

    /**
     * Fetch repositories accessible by user
     */
    public JsonNode getUserRepositories(String accessToken) {
        String url = properties.getGithub().getApiUrl() + "/user/repos?sort=updated&per_page=100";
        return makeGetRequest(url, accessToken);
    }

    /**
     * Fetch specific repository details
     */
    public JsonNode getRepository(String owner, String repo, String accessToken) {
        String url = String.format("%s/repos/%s/%s", properties.getGithub().getApiUrl(), owner, repo);
        return makeGetRequest(url, accessToken);
    }

    /**
     * Fetch pull request details including files changed
     */
    public JsonNode getPullRequest(String owner, String repo, int prNumber, String accessToken) {
        String url = String.format("%s/repos/%s/%s/pulls/%d", properties.getGithub().getApiUrl(), owner, repo, prNumber);
        return makeGetRequest(url, accessToken);
    }

    /**
     * Fetch pull request files list
     */
    public JsonNode getPullRequestFiles(String owner, String repo, int prNumber, String accessToken) {
        String url = String.format("%s/repos/%s/%s/pulls/%d/files?per_page=100", properties.getGithub().getApiUrl(), owner, repo, prNumber);
        return makeGetRequest(url, accessToken);
    }

    /**
     * Post a check run or status comment to GitHub PR
     */
    public void postPullRequestComment(String owner, String repo, int prNumber, String commentBody, String accessToken) {
        String url = String.format("%s/repos/%s/%s/issues/%d/comments", properties.getGithub().getApiUrl(), owner, repo, prNumber);
        Map<String, String> payload = Collections.singletonMap("body", commentBody);
        makePostRequest(url, payload, accessToken);
    }

    /**
     * Create GitHub Check Run for validation engine
     */
    public JsonNode createCheckRun(String owner, String repo, String headSha, String status, String conclusion, String title, String summary, String accessToken) {
        String url = String.format("%s/repos/%s/%s/check-runs", properties.getGithub().getApiUrl(), owner, repo);
        
        Map<String, Object> body = new HashMap<>();
        body.put("name", "SentryGate Validation");
        body.put("head_sha", headSha);
        body.put("status", status); // queued, in_progress, completed
        if (conclusion != null) {
            body.put("conclusion", conclusion); // success, failure, neutral, action_required
        }
        
        Map<String, Object> output = new HashMap<>();
        output.put("title", title);
        output.put("summary", summary);
        body.put("output", output);

        return makePostRequest(url, body, accessToken);
    }

    /**
     * Fetch open issues for a repository
     */
    public JsonNode getRepositoryIssues(String owner, String repo, String accessToken) {
        String url = String.format("%s/repos/%s/%s/issues?state=all&per_page=100", properties.getGithub().getApiUrl(), owner, repo);
        return makeGetRequest(url, accessToken);
    }

    private JsonNode makeGetRequest(String url, String token) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Failed to execute GitHub API GET request to {}: {}", url, e.getMessage());
            throw new GitHubApiException("GitHub API request failed: " + e.getMessage(), 500, e);
        }
    }

    private JsonNode makePostRequest(String url, Object body, String token) {
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<Object> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error("Failed to execute GitHub API POST request to {}: {}", url, e.getMessage());
            throw new GitHubApiException("GitHub API request failed: " + e.getMessage(), 500, e);
        }
    }

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/vnd.github.v3+json");
        if (token != null && !token.isBlank()) {
            headers.set("Authorization", "Bearer " + token);
        }
        headers.set("User-Agent", "SentryGate-Quality-Platform");
        return headers;
    }
}
