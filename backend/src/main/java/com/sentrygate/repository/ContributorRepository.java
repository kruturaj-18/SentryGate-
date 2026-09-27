package com.sentrygate.repository;

import com.sentrygate.entity.Contributor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContributorRepository extends JpaRepository<Contributor, Long> {
    List<Contributor> findByRepositoryId(Long repositoryId);
    Optional<Contributor> findByGithubIdAndRepositoryId(Long githubId, Long repositoryId);
    Optional<Contributor> findByLoginAndRepositoryId(String login, Long repositoryId);

    @Query("SELECT AVG(ts.overallScore) FROM TrustScore ts WHERE ts.contributor.repository.connectedUser.id = :userId")
    Double findAverageTrustScoreByUserId(@Param("userId") Long userId);
}
