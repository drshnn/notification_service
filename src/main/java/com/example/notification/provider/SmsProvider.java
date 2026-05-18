package com.example.notification.provider;

import com.example.notification.event.NotificationEvent;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class SmsProvider implements NotificationProvider {

    @Override
    @CircuitBreaker(name = "provider", fallbackMethod = "fallbackSend")
    public String send(NotificationEvent event, String compiledBody) {
        log.info("[MOCK TWILIO] Sending SMS to {} | Content: {}", event.getRecipient(), compiledBody);
        
        // Simulate network latency
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Return a mock provider ID
        return "tw-" + UUID.randomUUID().toString();
    }

    @Override
    public String getProviderName() {
        return "TWILIO_MOCK";
    }

    public String fallbackSend(NotificationEvent event, String compiledBody, Throwable t) {
        log.warn("[FALLBACK SMS] Circuit breaker OPEN. Simulating fallback provider for {}", event.getTrackingId());
        return "fallback-tw-" + UUID.randomUUID().toString();
    }
}
