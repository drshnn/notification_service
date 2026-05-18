# Notification Service: Architecture Overview

## 1. High-Level System Design

The Notification Service is designed as an event-driven microservice system that decouples the receipt of notification requests from the actual sending process. This approach ensures high availability, fault tolerance, and the ability to scale different notification channels (Email, SMS, Push) independently.

### Core Components

1. **API Gateway / Receiver**
   - A Spring Boot REST API that accepts incoming notification requests.
   - Responsible for synchronous validation (payload structure, authorization).
   - Generates an `idempotencyKey` (if not provided) and pushes the raw request to the Kafka Broker.
   - Returns a `202 Accepted` response with a tracking ID immediately.

2. **Apache Kafka (Event Broker)**
   - Acts as the central nervous system for the service.
   - Persists incoming requests in channel-specific topics, providing buffering during traffic spikes.
   - Ensures at-least-once delivery semantics for downstream processors.

3. **Template Engine Service**
   - Manages message templates (using Freemarker or Thymeleaf).
   - Abstracts the compilation of templates with dynamic variables.
   - Caches compiled templates in memory/Redis to reduce database hits.

4. **Worker Microservices (Consumers)**
   - **Email Worker:** Consumes from `notification.email`, fetches templates, and integrates with SMTP providers (e.g., AWS SES, SendGrid).
   - **SMS Worker:** Consumes from `notification.sms`, handles character encoding, and integrates with SMS Gateways (e.g., Twilio, AWS SNS).
   - **Push Worker:** Consumes from `notification.push`, integrates with FCM (Firebase Cloud Messaging) and APNs (Apple Push Notification service).

5. **Database (PostgreSQL / MongoDB)**
   - Stores configuration, templates, user preferences (opt-out lists), and immutable logs of notification states (PENDING, DELIVERED, FAILED).

---

## 2. Request Flow

1. **Submission:** A client (e.g., an Order Service) sends a `POST /api/v1/notifications` request specifying the channel, recipient, and template parameters.
2. **Validation:** The API Receiver validates the request. If valid, it persists a `PENDING` record in the DB and publishes a message to the relevant Kafka topic.
3. **Buffering:** Kafka securely buffers the message until a Worker is ready.
4. **Processing:**
   - The appropriate Worker consumes the message.
   - It checks user preferences to ensure the user hasn't opted out.
   - It requests the compiled content from the Template Engine.
   - It attempts to send the message via the 3rd Party Provider.
5. **Status Update:** Depending on the provider's synchronous response (or async webhook), the Worker publishes a status update event to a `notification.status` topic.
6. **Audit & Logs:** A dedicated Status Worker updates the Database record to `DELIVERED` or `FAILED`.

---

## 3. Production Patterns

- **Decoupling via Kafka:** Prevents slow 3rd party providers from blocking the main API threads.
- **Idempotency:** A unique `idempotency_key` is required for every request. The API layer checks Redis/DB for this key; if a duplicate is found within a 24-hour window, the previous result is returned, and the request is not re-processed.
- **Multi-Provider Failover:** The worker services implement a Circuit Breaker pattern (via Resilience4j). If the primary provider (e.g., SendGrid) experiences high failure rates or latency, the worker automatically switches traffic to a fallback provider (e.g., Amazon SES).
- **Graceful Degradation:** If the Template Database goes down, workers can rely on a local cache of recently used templates to continue processing.
