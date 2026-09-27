package com.sentrygate.service;

import com.sentrygate.dto.IssueDto;
import com.sentrygate.dto.IssueSimilarityDto;
import com.sentrygate.entity.Issue;
import com.sentrygate.entity.Repository;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.issue.DuplicateIssueDetector;
import com.sentrygate.repository.IssueRepository;
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
public class IssueService {

    private final IssueRepository issueRepository;
    private final RepositoryRepository repositoryRepository;
    private final DuplicateIssueDetector duplicateIssueDetector;

    @Transactional(readOnly = true)
    public List<IssueDto> getIssuesForRepo(UUID repoId) {
        return issueRepository.findByRepositoryId(repoId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public IssueDto getIssueById(UUID issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("Issue", "id", issueId));
        
        List<IssueSimilarityDto> similarities = duplicateIssueDetector.analyzeDuplicates(issue, 0.30);
        IssueDto dto = mapToDto(issue);
        dto.setDuplicateMatches(similarities);
        return dto;
    }

    @Transactional
    public IssueDto saveIssueAndDetectDuplicates(Issue issue) {
        Issue saved = issueRepository.save(issue);
        List<IssueSimilarityDto> similarities = duplicateIssueDetector.analyzeDuplicates(saved, 0.30);

        boolean hasHighDuplicate = similarities.stream().anyMatch(IssueSimilarityDto::getIsDuplicateCandidate);
        saved.setHasDuplicateCandidates(hasHighDuplicate);
        issueRepository.save(saved);

        IssueDto dto = mapToDto(saved);
        dto.setDuplicateMatches(similarities);
        return dto;
    }

    public IssueDto mapToDto(Issue issue) {
        return IssueDto.builder()
                .id(issue.getId())
                .repositoryId(issue.getRepository().getId())
                .githubIssueId(issue.getGithubIssueId())
                .issueNumber(issue.getIssueNumber())
                .title(issue.getTitle())
                .body(issue.getBody())
                .state(issue.getState())
                .authorUsername(issue.getAuthorUsername())
                .authorAvatarUrl(issue.getAuthorAvatarUrl())
                .hasDuplicateCandidates(issue.getHasDuplicateCandidates())
                .createdAt(issue.getCreatedAt())
                .updatedAt(issue.getUpdatedAt())
                .closedAt(issue.getClosedAt())
                .build();
    }
}
