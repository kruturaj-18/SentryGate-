package com.sentrygate.service;

import com.sentrygate.dto.PullRequestValidationDto;
import com.sentrygate.dto.ValidationResultDto;
import com.sentrygate.entity.PullRequest;
import com.sentrygate.entity.PullRequestValidation;
import com.sentrygate.entity.Repository;
import com.sentrygate.entity.ValidationResult;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.github.GitHubClient;
import com.sentrygate.repository.PullRequestRepository;
import com.sentrygate.repository.PullRequestValidationRepository;
import com.sentrygate.repository.RepositoryRepository;
import com.sentrygate.repository.ValidationResultRepository;
import com.sentrygate.validation.RuleResult;
import com.sentrygate.validation.ValidationContext;
import com.sentrygate.validation.ValidationEngine;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
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
    private final RepositoryRepository repositoryRepository;
    private final GitHubClient gitHubClient;

    @Transactional
    public PullRequestValidationDto validatePullRequest(UUID prId) {
        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(() -> new ResourceNotFoundException("PullRequest", "id", prId));

        return executeValidation(pr);
    }

    @Transactional
    public PullRequestValidationDto executeValidation(PullRequest pr) {
        log.info("Executing validation gate for PR #{} ({}) in repo {}", 
                pr.getPrNumber(), pr.getTitle(), pr.getRepository().getFullName());

        ValidationContext context = ValidationContext.builder()
                .prTitle(pr.getTitle())
                .prBody(pr.getBody())
                .sourceBranch(pr.getSourceBranch())
                .targetBranch(pr.getTargetBranch())
                .changedFilesCount(pr.getChangedFilesCount() != null ? pr.getChangedFilesCount() : 0)
                .additions(pr.getAdditions() != null ? pr.getAdditions() : 0)
                .deletions(pr.getDeletions() != null ? pr.getDeletions() : 0)
                .authorUsername(pr.getAuthorUsername())
                .headSha(pr.getHeadSha())
                .changedFilesList(Collections.emptyList()) // Can be populated from GitHub if token available
                .build();

        List<RuleResult> ruleResults = validationEngine.evaluateAll(context, pr.getRepository().getId());

        boolean overallPassed = ruleResults.stream().allMatch(RuleResult::isPassed);
        long totalScore = Math.round(ruleResults.stream().mapToDouble(RuleResult::getScore).average().orElse(100.0));

        PullRequestValidation validation = PullRequestValidation.builder()
                .pullRequest(pr)
                .passed(overallPassed)
                .score(BigDecimal.valueOf(totalScore))
                .evaluatedAt(OffsetDateTime.now())
                .summary(String.format("Evaluated %d rules: %s (%d/100)", 
                        ruleResults.size(), overallPassed ? "PASSED" : "FAILED", totalScore))
                .build();

        PullRequestValidation savedValidation = validationRepository.save(validation);

        List<ValidationResult> results = new ArrayList<>();
        for (RuleResult rr : ruleResults) {
            ValidationResult vr = ValidationResult.builder()
                    .validation(savedValidation)
                    .ruleCode(rr.getRuleCode())
                    .passed(rr.isPassed())
                    .score(BigDecimal.valueOf(rr.getScore()))
                    .message(rr.getMessage())
                    .details(rr.getDetails() != null ? rr.getDetails().toString() : null)
                    .executedAt(rr.getExecutedAt())
                    .build();
            results.add(vr);
        }
        resultRepository.saveAll(results);

        // Update PR overall validation status
        pr.setValidationStatus(overallPassed ? "PASSED" : "FAILED");
        pullRequestRepository.save(pr);

        // Post status back to GitHub if repository has user with access token
        try {
            if (pr.getRepository().getOwnerUser() != null && pr.getRepository().getOwnerUser().getGithubAccessToken() != null) {
                String commentText = buildCommentSummary(overallPassed, totalScore, ruleResults);
                gitHubClient.postPullRequestComment(
                        pr.getRepository().getOwner(),
                        pr.getRepository().getName(),
                        pr.getPrNumber(),
                        commentText,
                        pr.getRepository().getOwnerUser().getGithubAccessToken()
                );
            }
        } catch (Exception e) {
            log.warn("Could not post GitHub comment for PR #{}: {}", pr.getPrNumber(), e.getMessage());
        }

        return mapToDto(savedValidation, results);
    }

    @Transactional(readOnly = true)
    public List<PullRequestValidationDto> getValidationHistoryForPr(UUID prId) {
        List<PullRequestValidation> validations = validationRepository.findByPullRequestIdOrderByEvaluatedAtDesc(prId);
        return validations.stream()
                .map(v -> {
                    List<ValidationResult> results = resultRepository.findByValidationId(v.getId());
                    return mapToDto(v, results);
                })
                .collect(Collectors.toList());
    }

    private String buildCommentSummary(boolean passed, long score, List<RuleResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 🛡️ SentryGate Quality Check\n\n");
        sb.append(String.format("**Status:** %s  \n", passed ? "✅ **PASSED**" : "❌ **FAILED**"));
        sb.append(String.format("**Quality Score:** `%d / 100`\n\n", score));
        sb.append("| Rule | Status | Score | Details |\n");
        sb.append("| :--- | :---: | :---: | :--- |\n");

        for (RuleResult r : results) {
            sb.append(String.format("| `%s` | %s | %d | %s |\n",
                    r.getRuleCode(),
                    r.isPassed() ? "✅ Pass" : "❌ Fail",
                    Math.round(r.getScore()),
                    r.getMessage()));
        }

        sb.append("\n*Powered by SentryGate Automated Quality Engine.*");
        return sb.toString();
    }

    private PullRequestValidationDto mapToDto(PullRequestValidation v, List<ValidationResult> results) {
        List<ValidationResultDto> resultDtos = results.stream()
                .map(r -> ValidationResultDto.builder()
                        .id(r.getId())
                        .ruleCode(r.getRuleCode())
                        .passed(r.getPassed())
                        .score(r.getScore())
                        .message(r.getMessage())
                        .details(r.getDetails())
                        .executedAt(r.getExecutedAt())
                        .build())
                .collect(Collectors.toList());

        return PullRequestValidationDto.builder()
                .id(v.getId())
                .pullRequestId(v.getPullRequest().getId())
                .passed(v.getPassed())
                .score(v.getScore())
                .summary(v.getSummary())
                .evaluatedAt(v.getEvaluatedAt())
                .results(resultDtos)
                .build();
    }
}
