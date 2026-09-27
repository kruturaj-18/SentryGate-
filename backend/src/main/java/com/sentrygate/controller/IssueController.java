package com.sentrygate.controller;

import com.sentrygate.dto.IssueDto;
import com.sentrygate.service.IssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/issues")
@RequiredArgsConstructor
public class IssueController {

    private final IssueService issueService;

    @GetMapping("/repo/{repoId}")
    public ResponseEntity<List<IssueDto>> getIssuesForRepo(@PathVariable UUID repoId) {
        return ResponseEntity.ok(issueService.getIssuesForRepo(repoId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IssueDto> getIssueById(@PathVariable UUID id) {
        return ResponseEntity.ok(issueService.getIssueById(id));
    }
}
