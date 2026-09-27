package com.sentrygate.repository;

import com.sentrygate.entity.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RepositoryRepository extends JpaRepository<Repository, Long> {
    Optional<Repository> findByGithubRepoId(Long githubRepoId);
    Optional<Repository> findByFullName(String fullName);
    List<Repository> findByConnectedUserId(Long userId);
    List<Repository> findByConnectedUserIdAndMonitoringStatus(Long userId, String status);
    Page<Repository> findByConnectedUserId(Long userId, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Repository r WHERE r.connectedUser.id = :userId")
    long countByConnectedUserId(@Param("userId") Long userId);
}
