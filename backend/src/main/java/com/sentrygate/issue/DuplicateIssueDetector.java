package com.sentrygate.issue;

import com.sentrygate.dto.IssueSimilarityDto;
import com.sentrygate.entity.Issue;
import com.sentrygate.entity.IssueSimilarity;
import com.sentrygate.repository.IssueRepository;
import com.sentrygate.repository.IssueSimilarityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DuplicateIssueDetector {

    private final IssueRepository issueRepository;
    private final IssueSimilarityRepository issueSimilarityRepository;

    private static final Pattern STOP_WORDS = Pattern.compile(
            "\\b(a|an|and|are|as|at|be|by|for|from|has|he|in|is|it|its|of|on|that|the|to|was|were|will|with|this|my|not|have|bug|issue|error|problem|fix|help|please)\\b",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Check a target issue against all existing issues in the same repository
     */
    @Transactional
    public List<IssueSimilarityDto> analyzeDuplicates(Issue targetIssue, double threshold) {
        log.info("Analyzing potential duplicate issues for issue #{} in repository {}", 
                targetIssue.getNumber(), targetIssue.getRepository().getName());

        List<Issue> existingIssues = issueRepository.findByRepositoryId(targetIssue.getRepository().getId());
        List<IssueSimilarityDto> matches = new ArrayList<>();

        Set<String> targetTokens = extractTokens(targetIssue.getTitle() + " " + Objects.requireNonNullElse(targetIssue.getBody(), ""));

        for (Issue existing : existingIssues) {
            // Don't compare issue with itself
            if (existing.getId().equals(targetIssue.getId())) {
                continue;
            }

            Set<String> existingTokens = extractTokens(existing.getTitle() + " " + Objects.requireNonNullElse(existing.getBody(), ""));

            double similarity = calculateJaccardSimilarity(targetTokens, existingTokens);
            double titleSimilarity = calculateLevenshteinSimilarity(
                    targetIssue.getTitle().toLowerCase(), 
                    existing.getTitle().toLowerCase()
            );

            // Combined similarity score (weighted title + body)
            double combinedScore = (similarity * 0.4) + (titleSimilarity * 0.6);

            if (combinedScore >= threshold) {
                BigDecimal scoreBd = BigDecimal.valueOf(combinedScore).setScale(4, RoundingMode.HALF_UP);

                // Save or update similarity record in DB
                IssueSimilarity sim = issueSimilarityRepository.findBySourceIssueIdAndTargetIssueId(targetIssue.getId(), existing.getId())
                        .orElse(IssueSimilarity.builder()
                                .sourceIssue(targetIssue)
                                .targetIssue(existing)
                                .build());

                sim.setSimilarityScore(scoreBd);
                issueSimilarityRepository.save(sim);

                matches.add(IssueSimilarityDto.builder()
                        .id(sim.getId())
                        .sourceIssueId(targetIssue.getId())
                        .targetIssueId(existing.getId())
                        .targetIssueNumber(existing.getNumber())
                        .targetIssueTitle(existing.getTitle())
                        .similarityScore(scoreBd)
                        .method(sim.getMethod())
                        .createdAt(sim.getCreatedAt())
                        .build());
            }
        }

        matches.sort((a, b) -> b.getSimilarityScore().compareTo(a.getSimilarityScore()));
        return matches;
    }

    private Set<String> extractTokens(String text) {
        if (text == null || text.isBlank()) return Collections.emptySet();

        String cleaned = STOP_WORDS.matcher(text.toLowerCase()).replaceAll("");
        String[] words = cleaned.replaceAll("[^a-zA-Z0-9\\s]", " ").split("\\s+");

        return Arrays.stream(words)
                .filter(w -> w.length() > 2)
                .collect(Collectors.toSet());
    }

    private double calculateJaccardSimilarity(Set<String> set1, Set<String> set2) {
        if (set1.isEmpty() || set2.isEmpty()) return 0.0;

        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);

        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);

        return (double) intersection.size() / union.size();
    }

    private double calculateLevenshteinSimilarity(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;

        int distance = computeLevenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLength);
    }

    private int computeLevenshteinDistance(String s1, String s2) {
        int[] costs = new int[s2.length() + 1];
        for (int j = 0; j <= s2.length(); j++) {
            costs[j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(
                        1 + Math.min(costs[j], costs[j - 1]),
                        s1.charAt(i - 1) == s2.charAt(j - 1) ? nw : nw + 1
                );
                nw = costs[j];
                costs[j] = cj;
            }
        }

        return costs[s2.length()];
    }
}
