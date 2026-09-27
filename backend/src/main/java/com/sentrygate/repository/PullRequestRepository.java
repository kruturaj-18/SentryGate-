package com.sentrygate.repository;

import com.sentrygate.entity.PullRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PullRequestRepository extends JpaRepository<PullRequest, Long> {
    Optional<PullRequest> findByRepositoryIdAndNumber(Long repositoryId, Integer number);
    Optional<PullRequest> findByGithubPrId(Long githubPrId);
    Page<PullRequest> findByRepositoryId(Long repositoryId, Pageable pageable);
    List<PullRequest> findByRepositoryIdAndState(Long repositoryId, String state);
    long countByRepositoryIdAndState(Long repositoryId, String state);

    @Query("SELECT COUNT(p) FROM PullRequest p WHERE p.repository.connectedUser.id = :userId AND p.state = 'open'")
    long countOpenByUserId(@Param("userId") Long userId);
}
