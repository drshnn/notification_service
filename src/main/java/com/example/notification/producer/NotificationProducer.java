package com.example.notification.producer;

import com.example.notification.config.KafkaTopicConfig;
import com.example.notification.domain.enums.Channel;
import com.example.notification.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendNotificationEvent(NotificationEvent event) {
        String topic = getTopicForChannel(event.getChannel());
        String key = event.getRecipient(); // Partition by recipient for ordering

        log.info("Publishing event to topic {}: {}", topic, event.getTrackingId());
        kafkaTemplate.send(topic, key, event);
    }

    private String getTopicForChannel(Channel channel) {
        return switch (channel) {
            case EMAIL -> KafkaTopicConfig.EMAIL_TOPIC;
            case SMS -> KafkaTopicConfig.SMS_TOPIC;
            case PUSH -> KafkaTopicConfig.PUSH_TOPIC;
        };
    }
}
