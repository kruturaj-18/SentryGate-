package com.sentrygate.service;

import com.sentrygate.dto.ActivityLogDto;
import com.sentrygate.entity.ActivityLog;
import com.sentrygate.entity.Repository;
import com.sentrygate.entity.User;
import com.sentrygate.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void logActivity(User user, Repository repository, String action, String description, String detailsJson) {
        ActivityLog logEntry = ActivityLog.builder()
                .user(user)
                .repository(repository)
                .action(action)
                .description(description)
                .detailsJson(detailsJson)
                .createdAt(OffsetDateTime.now())
                .build();
        activityLogRepository.save(logEntry);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogDto> getLogsForRepo(UUID repoId) {
        return activityLogRepository.findByRepositoryIdOrderByCreatedAtDesc(repoId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private ActivityLogDto mapToDto(ActivityLog log) {
        return ActivityLogDto.builder()
                .id(log.getId())
                .userId(log.getUser() != null ? log.getUser().getId() : null)
                .repositoryId(log.getRepository() != null ? log.getRepository().getId() : null)
                .action(log.getAction())
                .description(log.getDescription())
                .details(log.getDetailsJson())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
