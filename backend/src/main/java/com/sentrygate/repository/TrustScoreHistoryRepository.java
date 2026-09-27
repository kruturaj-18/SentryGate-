package com.sentrygate.repository;

import com.sentrygate.entity.TrustScoreHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrustScoreHistoryRepository extends JpaRepository<TrustScoreHistory, Long> {
    List<TrustScoreHistory> findByContributorIdOrderByRecordedAtAsc(Long contributorId);
    List<TrustScoreHistory> findTop12ByContributorIdOrderByRecordedAtDesc(Long contributorId);
}
