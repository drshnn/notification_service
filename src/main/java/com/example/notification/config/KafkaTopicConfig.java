package com.example.notification.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

@Configuration
public class KafkaTopicConfig {

    public static final String EMAIL_TOPIC = "notification.email.requests";
    public static final String SMS_TOPIC = "notification.sms.requests";
    public static final String PUSH_TOPIC = "notification.push.requests";
    public static final String STATUS_TOPIC = "notification.status.events";

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        ExponentialBackOff backOff = new ExponentialBackOff(2000L, 2.0);
        backOff.setMaxElapsedTime(15000L);
        return new DefaultErrorHandler(recoverer, backOff);
    }

    @Bean
    public NewTopic emailTopic() {
        return TopicBuilder.name(EMAIL_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic smsTopic() {
        return TopicBuilder.name(SMS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic pushTopic() {
        return TopicBuilder.name(PUSH_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic statusTopic() {
        return TopicBuilder.name(STATUS_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
