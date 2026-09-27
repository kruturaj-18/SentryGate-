package com.sentrygate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardSummaryDto {
    private long totalRepositories;
    private long totalOpenPullRequests;
    private long totalOpenIssues;
    private double validationPassRate;
    private double averageTrustScore;
    private long totalDuplicateIssues;
    private long totalValidationsToday;
    private List<ValidationTrendDto> validationTrend;
    private List<RepositoryDto> recentRepositories;
    private List<ActivityLogDto> recentActivity;
}
