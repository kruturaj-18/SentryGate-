package com.sentrygate.service;

import com.sentrygate.dto.PullRequestDto;
import com.sentrygate.entity.PullRequest;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.repository.PullRequestRepository;
import com.sentrygate.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestService {

    private final PullRequestRepository pullRequestRepository;
    private final RepositoryRepository repositoryRepository;

    @Transactional(readOnly = true)
    public List<PullRequestDto> getPullRequestsForRepo(Long repoId) {
        return pullRequestRepository.findByRepositoryId(repoId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PullRequestDto getPullRequestById(Long prId) {
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
                .repositoryFullName(pr.getRepository().getFullName())
                .githubPrId(pr.getGithubPrId())
                .number(pr.getNumber())
                .title(pr.getTitle())
                .body(pr.getBody())
                .state(pr.getState())
                .authorLogin(pr.getAuthorLogin())
                .headBranch(pr.getHeadBranch())
                .baseBranch(pr.getBaseBranch())
                .draft(pr.getDraft())
                .merged(pr.getMerged())
                .additions(pr.getAdditions())
                .deletions(pr.getDeletions())
                .changedFiles(pr.getChangedFiles())
                .htmlUrl(pr.getHtmlUrl())
                .githubCreatedAt(pr.getGithubCreatedAt())
                .githubUpdatedAt(pr.getGithubUpdatedAt())
                .createdAt(pr.getCreatedAt())
                .build();
    }
}
