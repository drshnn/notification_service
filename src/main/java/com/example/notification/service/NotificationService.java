package com.example.notification.service;

import com.example.notification.domain.entity.IdempotencyKey;
import com.example.notification.domain.entity.NotificationLog;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.dto.NotificationStatusResponse;
import com.example.notification.event.NotificationEvent;
import com.example.notification.producer.NotificationProducer;
import com.example.notification.repository.IdempotencyKeyRepository;
import com.example.notification.repository.NotificationLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final NotificationProducer notificationProducer;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public NotificationResponse processNotification(NotificationRequest request, String tenantId, String idempotencyKey) {
        // 0. Idempotency Check
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            Optional<IdempotencyKey> existingKey = idempotencyKeyRepository.findById(idempotencyKey);
            if (existingKey.isPresent()) {
                log.info("Idempotency key hit for {}. Returning cached response.", idempotencyKey);
                try {
                    return objectMapper.readValue(existingKey.get().getResponseBody(), NotificationResponse.class);
                } catch (JsonProcessingException e) {
                    log.error("Failed to parse cached response for key {}", idempotencyKey, e);
                    // fallback to reprocessing or throwing exception
                }
            }
        }

        // 1. Save Initial State to DB (PENDING)
        NotificationLog logEntry = NotificationLog.builder()
                .tenantId(tenantId)
                .recipient(request.getRecipient())
                .channel(request.getChannel())
                .status(NotificationStatus.PENDING)
                .build();
        
        logEntry = notificationLogRepository.save(logEntry);
        log.info("Saved PENDING notification log with ID: {}", logEntry.getId());

        // 2. Publish to Kafka
        NotificationEvent event = NotificationEvent.builder()
                .trackingId(logEntry.getId())
                .channel(request.getChannel())
                .recipient(request.getRecipient())
                .category(request.getCategory())
                .templateName(request.getTemplateName())
                .templateVariables(request.getTemplateVariables())
                .tenantId(tenantId)
                .build();

        notificationProducer.sendNotificationEvent(event);

        // 3. Return accepted response
        NotificationResponse response = NotificationResponse.builder()
                .trackingId(logEntry.getId())
                .status(NotificationStatus.PENDING)
                .message("Notification queued for processing.")
                .build();

        // 4. Save Idempotency Key
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            try {
                String responseBody = objectMapper.writeValueAsString(response);
                IdempotencyKey newKey = IdempotencyKey.builder()
                        .key(idempotencyKey)
                        .responseBody(responseBody)
                        .statusCode(202)
                        .expiresAt(LocalDateTime.now().plusHours(24))
                        .build();
                idempotencyKeyRepository.save(newKey);
            } catch (JsonProcessingException e) {
                log.error("Failed to serialize response for idempotency key {}", idempotencyKey, e);
            }
        }

        return response;
    }

    @Transactional(readOnly = true)
    public NotificationStatusResponse getStatus(UUID trackingId) {
        NotificationLog logEntry = notificationLogRepository.findById(trackingId)
                .orElseThrow(() -> new IllegalArgumentException("Tracking ID not found"));

        return NotificationStatusResponse.builder()
                .trackingId(logEntry.getId())
                .status(logEntry.getStatus())
                .channel(logEntry.getChannel())
                .provider(logEntry.getProvider())
                .createdAt(logEntry.getCreatedAt())
                .updatedAt(logEntry.getUpdatedAt())
                .errorMessage(logEntry.getErrorDetails())
                .build();
    }
}
