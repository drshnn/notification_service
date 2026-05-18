package com.example.notification.consumer;

import com.example.notification.config.KafkaTopicConfig;
import com.example.notification.domain.enums.Channel;
import com.example.notification.domain.enums.NotificationStatus;
import com.example.notification.event.NotificationEvent;
import com.example.notification.event.StatusUpdateEvent;
import com.example.notification.provider.EmailProvider;
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
public class EmailWorker {

    private final TemplateService templateService;
    private final EmailProvider emailProvider;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final UserPreferenceService userPreferenceService;

    @KafkaListener(topics = KafkaTopicConfig.EMAIL_TOPIC, groupId = "notification-email-group")
    public void consume(NotificationEvent event) {
        log.info("EmailWorker received event for tracking ID: {}", event.getTrackingId());

        StatusUpdateEvent statusEvent = StatusUpdateEvent.builder()
                .trackingId(event.getTrackingId())
                .providerName(emailProvider.getProviderName())
                .build();

        try {
            // 0. Check User Preferences (Opt-out)
            if (userPreferenceService.isOptedOut(event.getRecipient(), Channel.EMAIL, event.getCategory())) {
                log.info("User {} is opted out of {} for channel EMAIL. Skipping.", event.getRecipient(), event.getCategory());
                statusEvent.setStatus(NotificationStatus.FAILED);
                statusEvent.setErrorDetails("User opted out of this category.");
                return;
            }

            // 1. Resolve Template
            String body = templateService.resolveTemplate(
                    event.getTemplateName(),
                    Channel.EMAIL,
                    event.getTemplateVariables()
            );

            // 2. Send via Provider
            String providerMsgId = emailProvider.send(event, body);

            // 3. Update Status Event on Success
            statusEvent.setStatus(NotificationStatus.DELIVERED);
            statusEvent.setProviderMessageId(providerMsgId);

        } catch (Exception e) {
            log.error("Failed to process email for tracking ID: {}", event.getTrackingId(), e);
            // 4. Update Status Event on Failure
            statusEvent.setStatus(NotificationStatus.FAILED);
            statusEvent.setErrorDetails(e.getMessage());
        } finally {
            // 5. Publish Status Event to Kafka
            kafkaTemplate.send(KafkaTopicConfig.STATUS_TOPIC, event.getTrackingId().toString(), statusEvent);
        }
    }
}
