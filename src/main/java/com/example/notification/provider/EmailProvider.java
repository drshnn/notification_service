package com.example.notification.provider;

import com.example.notification.event.NotificationEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class EmailProvider implements NotificationProvider {

    @Override
    @CircuitBreaker(name = "provider", fallbackMethod = "fallbackSend")
    public String send(NotificationEvent event, String compiledBody) {
        log.info("[MOCK SENDGRID] Sending EMAIL to {} | Content: {}", event.getRecipient(), compiledBody);
        
        // Simulate network latency
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Return a mock provider ID
        return "sg-" + UUID.randomUUID().toString();
    }

    @Override
    public String getProviderName() {
        return "SENDGRID_MOCK";
    }

    public String fallbackSend(NotificationEvent event, String compiledBody, Throwable t) {
        log.warn("[FALLBACK EMAIL] Circuit breaker OPEN. Simulating fallback provider for {}", event.getTrackingId());
        return "fallback-sg-" + UUID.randomUUID().toString();
    }
}
