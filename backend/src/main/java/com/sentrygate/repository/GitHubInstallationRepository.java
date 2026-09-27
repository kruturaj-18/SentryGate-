package com.sentrygate.repository;

import com.sentrygate.entity.GitHubInstallation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GitHubInstallationRepository extends JpaRepository<GitHubInstallation, Long> {
    Optional<GitHubInstallation> findByInstallationId(Long installationId);
    Optional<GitHubInstallation> findByAccountLogin(String accountLogin);
}
