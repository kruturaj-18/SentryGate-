package com.sentrygate.controller;

import com.sentrygate.dto.ContributorDto;
import com.sentrygate.dto.TrustScoreDto;
import com.sentrygate.dto.TrustScoreHistoryDto;
import com.sentrygate.service.ContributorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contributors")
@RequiredArgsConstructor
public class ContributorController {

    private final ContributorService contributorService;

    @GetMapping("/repo/{repoId}")
    public ResponseEntity<List<ContributorDto>> getContributorsForRepo(@PathVariable Long repoId) {
        return ResponseEntity.ok(contributorService.getContributorsForRepo(repoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContributorDto> getContributorById(@PathVariable Long id) {
        return ResponseEntity.ok(contributorService.getContributorById(id));
    }

    @PostMapping("/{id}/calculate-trust")
    public ResponseEntity<TrustScoreDto> calculateTrustScore(@PathVariable Long id) {
        return ResponseEntity.ok(contributorService.calculateAndUpdateTrustScore(id));
    }

    @GetMapping("/{id}/trust-history")
    public ResponseEntity<List<TrustScoreHistoryDto>> getTrustScoreHistory(@PathVariable Long id) {
        return ResponseEntity.ok(contributorService.getTrustScoreHistory(id));
    }
}
