# Notification Service: Kafka Messaging Design

Kafka is the core infrastructural piece that ensures our notification service is resilient, scalable, and decoupled.

## 1. Topic Strategy

Instead of a single unified topic, we will use dedicated topics based on the notification channel. This allows us to scale consumers independently (e.g., scaling up Email consumers during a marketing campaign without affecting transactional SMS).

### Core Topics
- `notification.email.requests`
- `notification.sms.requests`
- `notification.push.requests`

**Partitions & Keys:**
- **Partitions:** Start with 6-12 partitions per topic to allow concurrent processing by multiple worker instances.
- **Routing Key:** Use the `recipient_id` or `tenant_id` as the Kafka message key. This guarantees that notifications for a specific user are processed in order, preventing scenarios where an "Order Cancelled" notification arrives before "Order Confirmed".

---

## 2. Delivery Guarantees

We aim for **At-Least-Once** delivery.
- **Producers (API Gateway):** Configured with `acks=all` and retries enabled to ensure messages are safely written to the Kafka cluster before responding `202 Accepted`.
- **Consumers (Workers):** Manual offset commits. A consumer will only commit its offset *after* it has successfully sent the notification and (optionally) published the status event.

---

## 3. Dead Letter Queues (DLQ) & Poison Pills

Messages that cannot be processed permanently (e.g., malformed payload, missing mandatory template variables) are known as "poison pills".
- If a consumer encounters a permanent failure, it will publish the message to a channel-specific DLQ topic (e.g., `notification.email.dlq`).
- The offset for the main topic is then committed to allow processing of subsequent messages.
- An alert is triggered on DLQ depth so engineers can manually inspect and optionally replay the failed messages.

---

## 4. Retries & Backoff Strategy

Transient failures (e.g., rate limits from Twilio, temporary network timeouts) must be retried. We will implement a **Delayed Retry Topic** pattern.

1. **Main Topic (`notification.sms.requests`)**: Initial consumption attempt.
2. **Retry Topic 1 (`notification.sms.retry-1`)**: If attempt 1 fails transiently, message is published here. A consumer with a delayed consumption logic (or leveraging a scheduler/time-wheel mechanism) reads this topic after 1 minute.
3. **Retry Topic 2 (`notification.sms.retry-2`)**: If attempt 2 fails, it's published here with a 5-minute delay.
4. **Retry Topic 3 (`notification.sms.retry-3`)**: Final attempt, 15-minute delay.
5. **DLQ**: If all retries are exhausted, the message is routed to the DLQ.

This exponential backoff prevents our system from hammering 3rd party providers when they are degraded.

---

## 5. Status Tracking via Events

To decouple the writing of DB logs from the sending action:
- Upon success or final failure, workers publish to a `notification.status.events` topic.
- A dedicated **Status Logger Consumer** reads this topic and updates the PostgreSQL database. This isolates database latency/downtime from the critical path of sending notifications.
