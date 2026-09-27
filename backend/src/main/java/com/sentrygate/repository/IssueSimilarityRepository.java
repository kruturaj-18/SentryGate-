package com.sentrygate.repository;

import com.sentrygate.entity.IssueSimilarity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IssueSimilarityRepository extends JpaRepository<IssueSimilarity, Long> {
    Optional<IssueSimilarity> findBySourceIssueIdAndTargetIssueId(Long sourceIssueId, Long targetIssueId);
    List<IssueSimilarity> findBySourceIssueIdOrderBySimilarityScoreDesc(Long sourceIssueId);
    List<IssueSimilarity> findBySourceIssueIdAndSimilarityScoreGreaterThanOrderBySimilarityScoreDesc(Long sourceIssueId, java.math.BigDecimal threshold);
    void deleteBySourceIssueId(Long sourceIssueId);
}
