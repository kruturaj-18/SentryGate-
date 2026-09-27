package com.sentrygate.service;

import com.sentrygate.dto.PullRequestDto;
import com.sentrygate.entity.PullRequest;
import com.sentrygate.entity.Repository;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.repository.PullRequestRepository;
import com.sentrygate.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestService {

    private final PullRequestRepository pullRequestRepository;
    private final RepositoryRepository repositoryRepository;

    @Transactional(readOnly = true)
    public List<PullRequestDto> getPullRequestsForRepo(UUID repoId) {
        return pullRequestRepository.findByRepositoryId(repoId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PullRequestDto getPullRequestById(UUID prId) {
        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(() -> new ResourceNotFoundException("PullRequest", "id", prId));
        return mapToDto(pr);
    }

    @Transactional
    public PullRequest saveOrUpdatePullRequest(PullRequest pr) {
        return pullRequestRepository.save(pr);
    }

    public PullRequestDto mapToDto(PullRequest pr) {
        return PullRequestDto.builder()
                .id(pr.getId())
                .repositoryId(pr.getRepository().getId())
                .githubPrId(pr.getGithubPrId())
                .prNumber(pr.getPrNumber())
                .title(pr.getTitle())
                .body(pr.getBody())
                .state(pr.getState())
                .authorUsername(pr.getAuthorUsername())
                .authorAvatarUrl(pr.getAuthorAvatarUrl())
                .sourceBranch(pr.getSourceBranch())
                .targetBranch(pr.getTargetBranch())
                .headSha(pr.getHeadSha())
                .validationStatus(pr.getValidationStatus())
                .additions(pr.getAdditions())
                .deletions(pr.getDeletions())
                .changedFilesCount(pr.getChangedFilesCount())
                .createdAt(pr.getCreatedAt())
                .updatedAt(pr.getUpdatedAt())
                .closedAt(pr.getClosedAt())
                .mergedAt(pr.getMergedAt())
                .build();
    }
}
