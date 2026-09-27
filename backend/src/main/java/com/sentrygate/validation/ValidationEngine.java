package com.sentrygate.validation;

import com.sentrygate.entity.*;
import com.sentrygate.repository.*;
import com.sentrygate.validation.rules.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Core validation engine that orchestrates all validation rules.
 * Rules are executed independently and results are aggregated.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ValidationEngine {

    private final RepositoryRuleRepository ruleRepository;
    private final PullRequestValidationRepository validationRepository;
    private final ValidationResultRepository validationResultRepository;

    // All registered validation rules (autowired from Spring context)
    private final List<ValidationRule> allRules;

    /**
     * Run full validation for a pull request.
     * @param pullRequest the PR to validate
     * @param changedFileNames list of files changed in this PR
     * @param triggeredBy what triggered this validation (WEBHOOK, MANUAL, etc.)
     * @return the completed validation record
     */
    @Transactional
    public PullRequestValidation runValidation(
            PullRequest pullRequest,
            List<String> changedFileNames,
            String triggeredBy) {

        log.info("Running validation for PR #{} in {}", pullRequest.getNumber(),
                pullRequest.getRepository().getFullName());

        // Load enabled rules for this repository
        List<RepositoryRule> repoRules = ruleRepository
                .findByRepositoryIdAndEnabled(pullRequest.getRepository().getId(), true);

        // Build rule config map: ruleCode -> config
        Map<String, Map<String, Object>> ruleConfigs = repoRules.stream()
                .collect(Collectors.toMap(
                        RepositoryRule::getRuleCode,
                        r -> r.getConfiguration() != null ? r.getConfiguration() : Map.of()
                ));

        // Set of enabled rule codes
        Set<String> enabledRuleCodes = repoRules.stream()
                .map(RepositoryRule::getRuleCode)
                .collect(Collectors.toSet());

        // Create validation record
        PullRequestValidation validation = PullRequestValidation.builder()
                .pullRequest(pullRequest)
                .overallStatus("RUNNING")
                .triggeredBy(triggeredBy)
                .startedAt(OffsetDateTime.now())
                .build();
        validation = validationRepository.save(validation);

        List<RuleResult> results = new ArrayList<>();

        // Execute each registered rule
        for (ValidationRule rule : allRules) {
            String ruleCode = rule.getRuleCode();

            // Skip rules not enabled for this repository
            if (!enabledRuleCodes.isEmpty() && !enabledRuleCodes.contains(ruleCode)) {
                results.add(RuleResult.skipped(ruleCode, rule.getRuleName(),
                        "Rule not enabled for this repository."));
                continue;
            }

            Map<String, Object> config = ruleConfigs.getOrDefault(ruleCode, Map.of());
            ValidationContext context = new ValidationContext(
                    pullRequest.getTitle(),
                    pullRequest.getBody(),
                    pullRequest.getHeadBranch(),
                    pullRequest.getBaseBranch(),
                    pullRequest.getAdditions(),
                    pullRequest.getDeletions(),
                    pullRequest.getChangedFiles(),
                    Boolean.TRUE.equals(pullRequest.getDraft()),
                    changedFileNames,
                    config
            );

            try {
                RuleResult result = rule.validate(context);
                results.add(result);
                log.debug("Rule {} → {} for PR #{}", ruleCode, result.getStatus(), pullRequest.getNumber());
            } catch (Exception e) {
                log.error("Rule {} threw exception for PR #{}: {}", ruleCode, pullRequest.getNumber(), e.getMessage());
                results.add(RuleResult.fail(ruleCode, rule.getRuleName(), "HIGH",
                        "Rule execution error: " + e.getMessage()));
            }
        }

        // Aggregate counts
        long passCount = results.stream().filter(r -> "PASS".equals(r.getStatus())).count();
        long failCount = results.stream().filter(r -> "FAIL".equals(r.getStatus())).count();
        long warnCount = results.stream().filter(r -> "WARNING".equals(r.getStatus())).count();
        long skipCount = results.stream().filter(r -> "SKIPPED".equals(r.getStatus())).count();

        // Determine overall status
        String overallStatus = failCount > 0 ? "FAILED" : warnCount > 0 ? "PASSED_WITH_WARNINGS" : "PASSED";

        // Save individual results
        final PullRequestValidation finalValidation = validation;
        List<ValidationResult> resultEntities = results.stream().map(r ->
            ValidationResult.builder()
                .validation(finalValidation)
                .ruleCode(r.getRuleCode())
                .ruleName(r.getRuleName())
                .status(r.getStatus())
                .severity(r.getSeverity())
                .message(r.getMessage())
                .details(r.getDetails())
                .build()
        ).collect(Collectors.toList());

        validationResultRepository.saveAll(resultEntities);

        // Update validation record
        validation.setOverallStatus(overallStatus);
        validation.setPassCount((int) passCount);
        validation.setFailCount((int) failCount);
        validation.setWarningCount((int) warnCount);
        validation.setSkippedCount((int) skipCount);
        validation.setCompletedAt(OffsetDateTime.now());
        validation = validationRepository.save(validation);

        log.info("Validation complete for PR #{}: {} (pass={}, fail={}, warn={})",
                pullRequest.getNumber(), overallStatus, passCount, failCount, warnCount);

        return validation;
    }
}
