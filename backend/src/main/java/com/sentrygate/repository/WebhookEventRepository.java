package com.sentrygate.repository;

import com.sentrygate.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookEventRepository extends JpaRepository<WebhookEvent, Long> {
    Optional<WebhookEvent> findByDeliveryId(String deliveryId);
    boolean existsByDeliveryId(String deliveryId);
}
