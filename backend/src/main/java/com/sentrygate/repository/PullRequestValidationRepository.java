package com.sentrygate.repository;

import com.sentrygate.entity.PullRequestValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PullRequestValidationRepository extends JpaRepository<PullRequestValidation, Long> {
    List<PullRequestValidation> findByPullRequestIdOrderByCreatedAtDesc(Long pullRequestId);
    Optional<PullRequestValidation> findTopByPullRequestIdOrderByCreatedAtDesc(Long pullRequestId);
    long countByOverallStatus(String overallStatus);

    @Query("SELECT COUNT(v) FROM PullRequestValidation v WHERE v.pullRequest.repository.connectedUser.id = :userId AND v.overallStatus = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    @Query("SELECT v FROM PullRequestValidation v WHERE v.pullRequest.repository.id = :repoId ORDER BY v.createdAt DESC")
    List<PullRequestValidation> findByRepositoryIdOrderByCreatedAtDesc(@Param("repoId") Long repoId);
}
