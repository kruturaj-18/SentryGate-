package com.sentrygate.service;

import com.sentrygate.dto.ContributorDto;
import com.sentrygate.dto.TrustScoreDto;
import com.sentrygate.dto.TrustScoreHistoryDto;
import com.sentrygate.entity.Contributor;
import com.sentrygate.entity.TrustScore;
import com.sentrygate.entity.TrustScoreHistory;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.repository.ContributorRepository;
import com.sentrygate.repository.TrustScoreHistoryRepository;
import com.sentrygate.repository.TrustScoreRepository;
import com.sentrygate.scoring.TrustScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContributorService {

    private final ContributorRepository contributorRepository;
    private final TrustScoreRepository trustScoreRepository;
    private final TrustScoreHistoryRepository historyRepository;
    private final TrustScoringService trustScoringService;

    @Transactional(readOnly = true)
    public List<ContributorDto> getContributorsForRepo(UUID repoId) {
        return contributorRepository.findByRepositoryId(repoId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContributorDto getContributorById(UUID contributorId) {
        Contributor contributor = contributorRepository.findById(contributorId)
                .orElseThrow(() -> new ResourceNotFoundException("Contributor", "id", contributorId));
        return mapToDto(contributor);
    }

    @Transactional
    public TrustScoreDto calculateAndUpdateTrustScore(UUID contributorId) {
        Contributor contributor = contributorRepository.findById(contributorId)
                .orElseThrow(() -> new ResourceNotFoundException("Contributor", "id", contributorId));

        TrustScoringService.ScoringResult result = trustScoringService.calculateScore(contributor);

        TrustScore trustScore = trustScoreRepository.findByContributorId(contributorId)
                .orElseGet(() -> TrustScore.builder()
                        .contributor(contributor)
                        .build());

        trustScore.setOverallScore(BigDecimal.valueOf(result.getOverallScore()));
        trustScore.setPrAcceptanceRate(BigDecimal.valueOf(result.getPrAcceptanceRate()));
        trustScore.setBuildSuccessRate(BigDecimal.valueOf(result.getBuildSuccessRate()));
        trustScore.setReviewQualityScore(BigDecimal.valueOf(result.getReviewQualityScore()));
        trustScore.setAccountAgeDays(result.getAccountAgeDays());
        trustScore.setRiskTier(result.getRiskTier());

        TrustScore saved = trustScoreRepository.save(trustScore);

        // Record history snapshot
        historyRepository.save(TrustScoreHistory.builder()
                .trustScore(saved)
                .score(saved.getOverallScore())
                .riskTier(saved.getRiskTier())
                .recordedAt(OffsetDateTime.now())
                .build());

        return mapToTrustScoreDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TrustScoreHistoryDto> getTrustScoreHistory(UUID contributorId) {
        TrustScore trustScore = trustScoreRepository.findByContributorId(contributorId)
                .orElseThrow(() -> new ResourceNotFoundException("TrustScore", "contributorId", contributorId));

        return historyRepository.findByTrustScoreIdOrderByRecordedAtAsc(trustScore.getId()).stream()
                .map(h -> TrustScoreHistoryDto.builder()
                        .id(h.getId())
                        .trustScoreId(h.getTrustScore().getId())
                        .score(h.getScore())
                        .riskTier(h.getRiskTier())
                        .recordedAt(h.getRecordedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private ContributorDto mapToDto(Contributor c) {
        TrustScore score = trustScoreRepository.findByContributorId(c.getId()).orElse(null);
        TrustScoreDto scoreDto = score != null ? mapToTrustScoreDto(score) : null;

        return ContributorDto.builder()
                .id(c.getId())
                .repositoryId(c.getRepository().getId())
                .username(c.getUsername())
                .avatarUrl(c.getAvatarUrl())
                .totalPrsSubmitted(c.getTotalPrsSubmitted())
                .prsMerged(c.getPrsMerged())
                .prsRejected(c.getPrsRejected())
                .successfulBuilds(c.getSuccessfulBuilds())
                .failedBuilds(c.getFailedBuilds())
                .githubAccountCreatedAt(c.getGithubAccountCreatedAt())
                .createdAt(c.getCreatedAt())
                .trustScore(scoreDto)
                .build();
    }

    private TrustScoreDto mapToTrustScoreDto(TrustScore s) {
        return TrustScoreDto.builder()
                .id(s.getId())
                .contributorId(s.getContributor().getId())
                .overallScore(s.getOverallScore())
                .prAcceptanceRate(s.getPrAcceptanceRate())
                .buildSuccessRate(s.getBuildSuccessRate())
                .reviewQualityScore(s.getReviewQualityScore())
                .accountAgeDays(s.getAccountAgeDays())
                .riskTier(s.getRiskTier())
                .calculatedAt(s.getCalculatedAt())
                .build();
    }
}
