package com.sentrygate.service;

import com.sentrygate.dto.ContributorDto;
import com.sentrygate.dto.TrustScoreDto;
import com.sentrygate.dto.TrustScoreHistoryDto;
import com.sentrygate.entity.Contributor;
import com.sentrygate.entity.TrustScore;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.repository.ContributorRepository;
import com.sentrygate.repository.TrustScoreHistoryRepository;
import com.sentrygate.repository.TrustScoreRepository;
import com.sentrygate.scoring.TrustScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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
    public List<ContributorDto> getContributorsForRepo(Long repoId) {
        return contributorRepository.findByRepositoryId(repoId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContributorDto getContributorById(Long contributorId) {
        Contributor contributor = contributorRepository.findById(contributorId)
                .orElseThrow(() -> new ResourceNotFoundException("Contributor", "id", contributorId));
        return mapToDto(contributor);
    }

    @Transactional
    public TrustScoreDto calculateAndUpdateTrustScore(Long contributorId) {
        Contributor contributor = contributorRepository.findById(contributorId)
                .orElseThrow(() -> new ResourceNotFoundException("Contributor", "id", contributorId));

        TrustScore saved = trustScoringService.calculateAndSave(contributor);
        return mapToTrustScoreDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TrustScoreHistoryDto> getTrustScoreHistory(Long contributorId) {
        return historyRepository.findByContributorIdOrderByRecordedAtAsc(contributorId).stream()
                .map(h -> TrustScoreHistoryDto.builder()
                        .id(h.getId())
                        .contributorId(contributorId)
                        .overallScore(h.getOverallScore())
                        .prSuccessScore(h.getPrSuccessScore())
                        .validationScore(h.getValidationScore())
                        .collaborationScore(h.getCollaborationScore())
                        .consistencyScore(h.getConsistencyScore())
                        .recordedAt(h.getRecordedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private ContributorDto mapToDto(Contributor c) {
        TrustScore score = trustScoreRepository.findByContributorId(c.getId()).orElse(null);
        TrustScoreDto scoreDto = score != null ? mapToTrustScoreDto(score) : null;

        return ContributorDto.builder()
                .id(c.getId())
                .githubId(c.getGithubId())
                .login(c.getLogin())
                .name(c.getName())
                .avatarUrl(c.getAvatarUrl())
                .repositoryId(c.getRepository() != null ? c.getRepository().getId() : null)
                .repositoryFullName(c.getRepository() != null ? c.getRepository().getFullName() : null)
                .totalPrs(c.getTotalPrs())
                .mergedPrs(c.getMergedPrs())
                .rejectedPrs(c.getRejectedPrs())
                .openPrs(c.getOpenPrs())
                .totalIssues(c.getTotalIssues())
                .closedIssues(c.getClosedIssues())
                .validationPasses(c.getValidationPasses())
                .validationFailures(c.getValidationFailures())
                .contributionCount(c.getContributionCount())
                .firstContributionAt(c.getFirstContributionAt())
                .lastContributionAt(c.getLastContributionAt())
                .trustScore(scoreDto)
                .createdAt(c.getCreatedAt())
                .build();
    }

    private TrustScoreDto mapToTrustScoreDto(TrustScore s) {
        return TrustScoreDto.builder()
                .id(s.getId())
                .contributorId(s.getContributor() != null ? s.getContributor().getId() : null)
                .contributorLogin(s.getContributor() != null ? s.getContributor().getLogin() : null)
                .overallScore(s.getOverallScore())
                .prSuccessScore(s.getPrSuccessScore())
                .validationScore(s.getValidationScore())
                .collaborationScore(s.getCollaborationScore())
                .consistencyScore(s.getConsistencyScore())
                .calculatedAt(s.getCalculatedAt())
                .build();
    }
}
