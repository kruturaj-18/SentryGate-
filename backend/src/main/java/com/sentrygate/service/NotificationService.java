package com.sentrygate.service;

import com.sentrygate.dto.NotificationDto;
import com.sentrygate.entity.Notification;
import com.sentrygate.entity.User;
import com.sentrygate.exception.ResourceNotFoundException;
import com.sentrygate.repository.NotificationRepository;
import com.sentrygate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NotificationDto> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationDto createNotification(Long userId, String type, String title, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Notification n = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .build();

        return mapToDto(notificationRepository.save(n));
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        n.setIsRead(true);
        notificationRepository.save(n);
    }

    private NotificationDto mapToDto(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .userId(n.getUser() != null ? n.getUser().getId() : null)
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .entityType(n.getEntityType())
                .entityId(n.getEntityId())
                .repositoryId(n.getRepository() != null ? n.getRepository().getId() : null)
                .repositoryName(n.getRepository() != null ? n.getRepository().getName() : null)
                .createdAt(n.getCreatedAt())
                .build();
    }
}
