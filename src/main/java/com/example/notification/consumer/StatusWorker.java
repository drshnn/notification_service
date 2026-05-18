package com.example.notification.consumer;

import com.example.notification.config.KafkaTopicConfig;
import com.example.notification.domain.entity.NotificationLog;
import com.example.notification.event.StatusUpdateEvent;
import com.example.notification.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatusWorker {

    private final NotificationLogRepository notificationLogRepository;

    @KafkaListener(topics = KafkaTopicConfig.STATUS_TOPIC, groupId = "notification-status-group")
    @Transactional
    public void consume(StatusUpdateEvent event) {
        log.info("StatusWorker received status update for tracking ID: {} to {}", event.getTrackingId(), event.getStatus());

        notificationLogRepository.findById(event.getTrackingId()).ifPresentOrElse(logEntry -> {
            logEntry.setStatus(event.getStatus());
            logEntry.setProvider(event.getProviderName());
            logEntry.setProviderMessageId(event.getProviderMessageId());
            logEntry.setErrorDetails(event.getErrorDetails());
            
            notificationLogRepository.save(logEntry);
            log.debug("Successfully updated NotificationLog {} to {}", event.getTrackingId(), event.getStatus());
        }, () -> log.warn("Received status update for unknown tracking ID: {}", event.getTrackingId()));
    }
}
