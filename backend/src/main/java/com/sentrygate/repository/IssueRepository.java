package com.sentrygate.repository;

import com.sentrygate.entity.Issue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {
    Optional<Issue> findByRepositoryIdAndNumber(Long repositoryId, Integer number);
    Page<Issue> findByRepositoryId(Long repositoryId, Pageable pageable);
    List<Issue> findByRepositoryIdAndState(Long repositoryId, String state);
    List<Issue> findByRepositoryIdAndIsPullRequest(Long repositoryId, Boolean isPullRequest);
    List<Issue> findByRepositoryIdAndDuplicateStatus(Long repositoryId, String duplicateStatus);
    long countByRepositoryIdAndDuplicateStatus(Long repositoryId, String duplicateStatus);
    long countByRepositoryIdAndState(Long repositoryId, String state);
}
