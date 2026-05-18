package com.example.notification.consumer;

import com.example.notification.config.KafkaTopicConfig;
import com.example.notification.domain.enums.Channel;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.event.NotificationEvent;
import com.example.notification.event.StatusUpdateEvent;
import com.example.notification.provider.PushProvider;
import com.example.notification.service.TemplateService;
import com.example.notification.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PushWorker {

    private final TemplateService templateService;
    private final PushProvider pushProvider;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserPreferenceService userPreferenceService;

    @KafkaListener(topics = KafkaTopicConfig.PUSH_TOPIC, groupId = "notification-push-group")
    public void consume(NotificationEvent event) {
        log.info("PushWorker received event for tracking ID: {}", event.getTrackingId());

        StatusUpdateEvent statusEvent = StatusUpdateEvent.builder()
                .trackingId(event.getTrackingId())
                .providerName(pushProvider.getProviderName())
                .build();

        try {
            if (userPreferenceService.isOptedOut(event.getRecipient(), Channel.PUSH, event.getCategory())) {
                log.info("User {} is opted out of {} for channel PUSH. Skipping.", event.getRecipient(), event.getCategory());
                statusEvent.setStatus(NotificationStatus.FAILED);
                statusEvent.setErrorDetails("User opted out of this category.");
                return;
            }

            String body = templateService.resolveTemplate(
                    event.getTemplateName(),
                    Channel.PUSH,
                    event.getTemplateVariables()
            );

            String providerMsgId = pushProvider.send(event, body);

            statusEvent.setStatus(NotificationStatus.DELIVERED);
            statusEvent.setProviderMessageId(providerMsgId);

        } catch (Exception e) {
            log.error("Failed to process Push for tracking ID: {}", event.getTrackingId(), e);
            statusEvent.setStatus(NotificationStatus.FAILED);
            statusEvent.setErrorDetails(e.getMessage());
        } finally {
            kafkaTemplate.send(KafkaTopicConfig.STATUS_TOPIC, event.getTrackingId().toString(), statusEvent);
        }
    }
}
