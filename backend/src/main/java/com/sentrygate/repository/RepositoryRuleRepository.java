package com.sentrygate.repository;

import com.sentrygate.entity.RepositoryRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryRuleRepository extends JpaRepository<RepositoryRule, Long> {
    List<RepositoryRule> findByRepositoryId(Long repositoryId);
    List<RepositoryRule> findByRepositoryIdAndEnabled(Long repositoryId, Boolean enabled);
    Optional<RepositoryRule> findByRepositoryIdAndRuleCode(Long repositoryId, String ruleCode);
    void deleteByRepositoryId(Long repositoryId);
}
