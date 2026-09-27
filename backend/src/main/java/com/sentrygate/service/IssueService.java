package com.sentrygate.service;

import com.sentrygate.dto.IssueDto;
import com.sentrygate.dto.IssueSimilarityDto;
import com.sentrygate.entity.Issue;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.issue.DuplicateIssueDetector;
import com.sentrygate.repository.IssueRepository;
import com.sentrygate.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueService {

    private final IssueRepository issueRepository;
    private final RepositoryRepository repositoryRepository;
    private final DuplicateIssueDetector duplicateIssueDetector;

    @Transactional(readOnly = true)
    public List<IssueDto> getIssuesForRepo(Long repoId) {
        return issueRepository.findByRepositoryId(repoId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IssueDto getIssueById(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));

        List<IssueSimilarityDto> similarities = duplicateIssueDetector.analyzeDuplicates(issue, 0.30);
        IssueDto dto = mapToDto(issue);
        dto.setSimilarities(similarities);
        return dto;
    }

    @Transactional
    public IssueDto saveIssueAndDetectDuplicates(Issue issue) {
        Issue saved = issueRepository.save(issue);
        List<IssueSimilarityDto> similarities = duplicateIssueDetector.analyzeDuplicates(saved, 0.30);

        boolean hasHighDuplicate = similarities.stream()
                .anyMatch(s -> s.getSimilarityScore() != null && s.getSimilarityScore().doubleValue() >= 0.70);
        if (hasHighDuplicate) {
            saved.setDuplicateStatus("DUPLICATE");
            issueRepository.save(saved);
        }

        IssueDto dto = mapToDto(saved);
        dto.setSimilarities(similarities);
        return dto;
    }

    public IssueDto mapToDto(Issue issue) {
        return IssueDto.builder()
                .id(issue.getId())
                .repositoryId(issue.getRepository() != null ? issue.getRepository().getId() : null)
                .repositoryFullName(issue.getRepository() != null ? issue.getRepository().getFullName() : null)
                .githubIssueId(issue.getGithubIssueId())
                .number(issue.getNumber())
                .title(issue.getTitle())
                .body(issue.getBody())
                .authorLogin(issue.getAuthorLogin())
                .state(issue.getState())
                .labels(issue.getLabels())
                .htmlUrl(issue.getHtmlUrl())
                .isPullRequest(issue.getIsPullRequest())
                .duplicateStatus(issue.getDuplicateStatus())
                .githubCreatedAt(issue.getGithubCreatedAt())
                .githubUpdatedAt(issue.getGithubUpdatedAt())
                .createdAt(issue.getCreatedAt())
                .build();
    }
}
