package com.sentrygate.scoring;

import com.sentrygate.entity.Contributor;
import com.sentrygate.entity.TrustScore;
import com.sentrygate.entity.TrustScoreHistory;
import com.sentrygate.repository.TrustScoreHistoryRepository;
import com.sentrygate.repository.TrustScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

/**
 * Trust Score calculation service.
 *
 * Formula (transparent and documented):
 *
 * 1. PR Success Score = (mergedPRs / max(totalPRs, 1)) * 100
 *    - Weighted: 0.35
 *
 * 2. Validation Score = (validationPasses / max(validationPasses + validationFailures, 1)) * 100
 *    - Weighted: 0.30
 *
 * 3. Collaboration Score = min(100, (closedIssues / max(totalIssues, 1) * 50) + (contributionCount * 2))
 *    - Weighted: 0.20
 *
 * 4. Consistency Score = based on how long they have been contributing (activity recency)
 *    - Weighted: 0.15
 *
 * Overall Score = (prSuccessScore * 0.35) + (validationScore * 0.30) +
 *                 (collaborationScore * 0.20) + (consistencyScore * 0.15)
 *
 * Score range: 0–100
 * New contributors start at 50.
 *
 * IMPORTANT: This score is an informational metric based on repository activity
 * and should NOT replace manual human review.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrustScoringService {

    private static final BigDecimal WEIGHT_PR_SUCCESS = BigDecimal.valueOf(0.35);
    private static final BigDecimal WEIGHT_VALIDATION = BigDecimal.valueOf(0.30);
    private static final BigDecimal WEIGHT_COLLABORATION = BigDecimal.valueOf(0.20);
    private static final BigDecimal WEIGHT_CONSISTENCY = BigDecimal.valueOf(0.15);
    private static final BigDecimal NEW_CONTRIBUTOR_BASELINE = BigDecimal.valueOf(50.0);

    private final TrustScoreRepository trustScoreRepository;
    private final TrustScoreHistoryRepository historyRepository;

    /**
     * Calculate and persist the trust score for a contributor.
     * Saves a history snapshot each time the score is recalculated.
     */
    @Transactional
    public TrustScore calculateAndSave(Contributor contributor) {
        BigDecimal prSuccessScore = calculatePrSuccessScore(contributor);
        BigDecimal validationScore = calculateValidationScore(contributor);
        BigDecimal collaborationScore = calculateCollaborationScore(contributor);
        BigDecimal consistencyScore = calculateConsistencyScore(contributor);

        BigDecimal overall = prSuccessScore.multiply(WEIGHT_PR_SUCCESS)
                .add(validationScore.multiply(WEIGHT_VALIDATION))
                .add(collaborationScore.multiply(WEIGHT_COLLABORATION))
                .add(consistencyScore.multiply(WEIGHT_CONSISTENCY))
                .setScale(2, RoundingMode.HALF_UP);

        // Clamp between 0 and 100
        overall = overall.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));

        // Save or update current score
        TrustScore score = trustScoreRepository.findByContributorId(contributor.getId())
                .orElse(TrustScore.builder().contributor(contributor).build());

        score.setOverallScore(overall);
        score.setPrSuccessScore(prSuccessScore);
        score.setValidationScore(validationScore);
        score.setCollaborationScore(collaborationScore);
        score.setConsistencyScore(consistencyScore);
        score.setCalculatedAt(OffsetDateTime.now());

        score = trustScoreRepository.save(score);

        // Persist history snapshot
        TrustScoreHistory history = TrustScoreHistory.builder()
                .contributor(contributor)
                .overallScore(overall)
                .prSuccessScore(prSuccessScore)
                .validationScore(validationScore)
                .collaborationScore(collaborationScore)
                .consistencyScore(consistencyScore)
                .build();
        historyRepository.save(history);

        log.info("Trust score for {}: {}/100 (PR={}, Val={}, Collab={}, Consist={})",
                contributor.getLogin(), overall, prSuccessScore, validationScore,
                collaborationScore, consistencyScore);

        return score;
    }

    private BigDecimal calculatePrSuccessScore(Contributor c) {
        int total = Math.max(c.getTotalPrs(), 1);
        int merged = c.getMergedPrs();
        // Penalize rejections
        double rawScore = ((double) merged / total * 100.0) - (c.getRejectedPrs() * 5.0);
        return clamp(rawScore);
    }

    private BigDecimal calculateValidationScore(Contributor c) {
        int total = c.getValidationPasses() + c.getValidationFailures();
        if (total == 0) {
            return NEW_CONTRIBUTOR_BASELINE; // Baseline for new contributors
        }
        double rawScore = (double) c.getValidationPasses() / total * 100.0;
        return clamp(rawScore);
    }

    private BigDecimal calculateCollaborationScore(Contributor c) {
        double issueScore = c.getTotalIssues() > 0
                ? (double) c.getClosedIssues() / c.getTotalIssues() * 50.0
                : 25.0; // Neutral if no issues
        double activityBonus = Math.min(50.0, c.getContributionCount() * 2.0);
        return clamp(issueScore + activityBonus);
    }

    private BigDecimal calculateConsistencyScore(Contributor c) {
        if (c.getFirstContributionAt() == null || c.getLastContributionAt() == null) {
            return NEW_CONTRIBUTOR_BASELINE;
        }
        // Recency: higher score for recent contributions
        long daysSinceLastContribution = java.time.temporal.ChronoUnit.DAYS.between(
                c.getLastContributionAt(), OffsetDateTime.now());
        double recencyScore = daysSinceLastContribution < 30 ? 100.0
                : daysSinceLastContribution < 90 ? 75.0
                : daysSinceLastContribution < 180 ? 50.0
                : 25.0;

        // Longevity: bonus for long-term contributors
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(
                c.getFirstContributionAt(), OffsetDateTime.now());
        double longevityBonus = Math.min(20.0, totalDays / 30.0);

        return clamp(recencyScore * 0.7 + longevityBonus * 1.5);
    }

    private BigDecimal clamp(double value) {
        double clamped = Math.max(0.0, Math.min(100.0, value));
        return BigDecimal.valueOf(clamped).setScale(2, RoundingMode.HALF_UP);
    }
}
