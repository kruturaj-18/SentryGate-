package com.sentrygate.controller;

import com.sentrygate.dto.PullRequestDto;
import com.sentrygate.dto.PullRequestValidationDto;
import com.sentrygate.service.PullRequestService;
import com.sentrygate.service.ValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pull-requests")
@RequiredArgsConstructor
public class PullRequestController {

    private final PullRequestService pullRequestService;
    private final ValidationService validationService;

    @GetMapping("/repo/{repoId}")
    public ResponseEntity<List<PullRequestDto>> getPullRequestsForRepo(@PathVariable Long repoId) {
        return ResponseEntity.ok(pullRequestService.getPullRequestsForRepo(repoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PullRequestDto> getPullRequestById(@PathVariable Long id) {
        return ResponseEntity.ok(pullRequestService.getPullRequestById(id));
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<PullRequestValidationDto> validatePullRequest(@PathVariable Long id) {
        return ResponseEntity.ok(validationService.validatePullRequest(id));
    }

    @GetMapping("/{id}/validations")
    public ResponseEntity<List<PullRequestValidationDto>> getValidationHistory(@PathVariable Long id) {
        return ResponseEntity.ok(validationService.getValidationHistoryForPr(id));
    }
}
