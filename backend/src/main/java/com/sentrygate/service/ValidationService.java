package com.sentrygate.service;

import com.sentrygate.dto.PullRequestValidationDto;
import com.sentrygate.dto.ValidationResultDto;
import com.sentrygate.entity.PullRequest;
import com.sentrygate.entity.PullRequestValidation;
import com.sentrygate.entity.ValidationResult;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.github.GitHubClient;
import com.sentrygate.repository.PullRequestRepository;
import com.sentrygate.repository.PullRequestValidationRepository;
import com.sentrygate.repository.ValidationResultRepository;
import com.sentrygate.validation.ValidationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationService {

    private final ValidationEngine validationEngine;
    private final PullRequestRepository pullRequestRepository;
    private final PullRequestValidationRepository validationRepository;
    private final ValidationResultRepository resultRepository;
    private final GitHubClient gitHubClient;

    // ------------------------------------------------------------------ //
    // Issue #1 fix: removed the broken evaluateAll() + manual entity-build
    // path that used non-existent fields (passed/score/summary/evaluatedAt).
    // Now delegates straight to ValidationEngine.runValidation() which owns
    // the full DB lifecycle.
    // ------------------------------------------------------------------ //

    @Transactional
    public PullRequestValidationDto validatePullRequest(Long prId) {
        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(() -> new ResourceNotFoundException("PullRequest", "id", prId));
        return executeValidation(pr, Collections.emptyList());
    }

    /**
     * Primary entry-point.
     * changedFileNames is pre-fetched from the GitHub Files API by the
     * webhook controller (Issue #2 fix) so file-based rules receive real data.
     */
    @Transactional
    public PullRequestValidationDto executeValidation(PullRequest pr, List<String> changedFileNames) {
        log.info("Executing validation gate for PR #{} ({}) in repo {}",
                pr.getNumber(), pr.getTitle(), pr.getRepository().getFullName());

        // Issue #1 fix: call runValidation() – the actual method that exists
        PullRequestValidation savedValidation =
                validationEngine.runValidation(pr, changedFileNames, "WEBHOOK");

        // Issue #4 fix: create a merge-blocking GitHub Check Run
        try {
            if (pr.getRepository().getConnectedUser() != null
                    && pr.getRepository().getConnectedUser().getGithubToken() != null
                    && pr.getHeadSha() != null) {

                String accessToken = pr.getRepository().getConnectedUser().getGithubToken();
                String owner       = pr.getRepository().getOwner();
                String repoName    = pr.getRepository().getName();
                String headSha     = pr.getHeadSha();
                String overallStatus = savedValidation.getOverallStatus();

                // Map internal status to GitHub Check conclusion
                String conclusion = switch (overallStatus) {
                    case "PASSED"               -> "success";
                    case "PASSED_WITH_WARNINGS" -> "neutral";
                    default                     -> "failure";  // FAILED / RUNNING
                };

                String title = "SentryGate: " + overallStatus;
                String summary = String.format(
                        "Pass: %d | Fail: %d | Warnings: %d | Skipped: %d",
                        savedValidation.getPassCount(),
                        savedValidation.getFailCount(),
                        savedValidation.getWarningCount(),
                        savedValidation.getSkippedCount());

                // Issue #4 fix: actually invoke createCheckRun()
                gitHubClient.createCheckRun(
                        owner, repoName, headSha,
                        "completed", conclusion,
                        title, summary,
                        accessToken);

                log.info("Created GitHub Check Run for PR #{}: {}", pr.getNumber(), conclusion);
            }
        } catch (Exception e) {
            log.warn("Could not create GitHub Check Run for PR #{}: {}", pr.getNumber(), e.getMessage());
        }

        List<ValidationResult> results = resultRepository.findByValidationId(savedValidation.getId());
        return mapToDto(savedValidation, results);
    }

    @Transactional(readOnly = true)
    public List<PullRequestValidationDto> getValidationHistoryForPr(Long prId) {
        List<PullRequestValidation> validations =
                validationRepository.findByPullRequestIdOrderByCreatedAtDesc(prId);
        return validations.stream()
                .map(v -> {
                    List<ValidationResult> results = resultRepository.findByValidationId(v.getId());
                    return mapToDto(v, results);
                })
                .collect(Collectors.toList());
    }

    private PullRequestValidationDto mapToDto(PullRequestValidation v, List<ValidationResult> results) {
        List<ValidationResultDto> resultDtos = results.stream()
                .map(r -> ValidationResultDto.builder()
                        .id(r.getId())
                        .ruleCode(r.getRuleCode())
                        .ruleName(r.getRuleName())
                        .status(r.getStatus())
                        .severity(r.getSeverity())
                        .message(r.getMessage())
                        .details(r.getDetails())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return PullRequestValidationDto.builder()
                .id(v.getId())
                .pullRequestId(v.getPullRequest().getId())
                .pullRequestNumber(v.getPullRequest().getNumber())
                .pullRequestTitle(v.getPullRequest().getTitle())
                .overallStatus(v.getOverallStatus())
                .passCount(v.getPassCount())
                .failCount(v.getFailCount())
                .warningCount(v.getWarningCount())
                .skippedCount(v.getSkippedCount())
                .triggeredBy(v.getTriggeredBy())
                .startedAt(v.getStartedAt())
                .completedAt(v.getCompletedAt())
                .createdAt(v.getCreatedAt())
                .results(resultDtos)
                .build();
    }
}
