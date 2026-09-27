package com.sentrygate.service;

import com.sentrygate.dto.DashboardSummaryDto;
import com.sentrygate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RepositoryRepository repositoryRepository;
    private final PullRequestRepository pullRequestRepository;
    private final IssueRepository issueRepository;
    private final ContributorRepository contributorRepository;
    private final PullRequestValidationRepository validationRepository;

    @Transactional(readOnly = true)
    public DashboardSummaryDto getDashboardSummary() {
        long totalRepos = repositoryRepository.count();
        long totalPrs = pullRequestRepository.count();
        long totalIssues = issueRepository.count();
        long totalContributors = contributorRepository.count();

        long passedValidations = validationRepository.countByPassed(true);
        long failedValidations = validationRepository.countByPassed(false);

        double passRate = (passedValidations + failedValidations > 0)
                ? ((double) passedValidations / (passedValidations + failedValidations)) * 100.0
                : 100.0;

        Map<String, Object> metrics = new HashMap<>();
        metrics.put("passedValidations", passedValidations);
        metrics.put("failedValidations", failedValidations);
        metrics.put("passRatePercentage", Math.round(passRate * 10.0) / 10.0);

        return DashboardSummaryDto.builder()
                .totalRepositories(totalRepos)
                .totalPullRequests(totalPrs)
                .totalIssues(totalIssues)
                .totalContributors(totalContributors)
                .passRatePercentage(passRate)
                .metrics(metrics)
                .build();
    }
}
