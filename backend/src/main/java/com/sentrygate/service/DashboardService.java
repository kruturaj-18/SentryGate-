package com.sentrygate.service;

import com.sentrygate.dto.DashboardSummaryDto;
import com.sentrygate.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

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
        long totalOpenPrs = pullRequestRepository.countByState("open");
        long totalOpenIssues = issueRepository.countByState("open");

        long passedValidations = validationRepository.countByOverallStatus("PASSED");
        long failedValidations = validationRepository.countByOverallStatus("FAILED");

        double passRate = (passedValidations + failedValidations > 0)
                ? ((double) passedValidations / (passedValidations + failedValidations)) * 100.0
                : 100.0;

        long totalDuplicates = issueRepository.countByDuplicateStatus("DUPLICATE");

        return DashboardSummaryDto.builder()
                .totalRepositories(totalRepos)
                .totalOpenPullRequests(totalOpenPrs)
                .totalOpenIssues(totalOpenIssues)
                .validationPassRate(Math.round(passRate * 10.0) / 10.0)
                .averageTrustScore(85.0)
                .totalDuplicateIssues(totalDuplicates)
                .totalValidationsToday(passedValidations + failedValidations)
                .validationTrend(Collections.emptyList())
                .recentRepositories(Collections.emptyList())
                .recentActivity(Collections.emptyList())
                .build();
    }
}
