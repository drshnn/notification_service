# Notification Service: Resilience & Monitoring

Operating a notification service at scale requires strict safeguards to prevent self-inflicted outages, protect 3rd party relationships, and ensure high visibility into system health.

## 1. Rate Limiting & Throttling

**Inbound API Gateway:**
- Use Redis-based rate limiting (e.g., token bucket algorithm) on `POST /api/v1/notifications`.
- Limit by `tenant_id` or `client_ip` to prevent a single tenant from starving the system.
- Return `429 Too Many Requests` when limits are breached.

**Outbound Provider Limits:**
- 3rd Party APIs (Twilio, SES) enforce their own rate limits.
- Kafka Consumers must implement client-side throttling (e.g., using Guava RateLimiter) tuned slightly below the provider's threshold.
- If a provider returns a `429`, the message must be routed to the Delay Retry Topic (Exponential Backoff).

---

## 2. Circuit Breakers & Failover

Use libraries like **Resilience4j** to wrap all outbound HTTP calls to providers.

- **Thresholds:** If 50% of requests fail within a 60-second sliding window, trip the breaker to `OPEN`.
- **Failover Logic:** 
  - Primary: SendGrid.
  - Fallback: If SendGrid breaker is `OPEN`, automatically route requests to AWS SES.
  - This prevents accumulating massive backlogs in Kafka while the primary provider is down.
- **Half-Open:** Every 30 seconds, allow a few test requests through to the primary provider to check if it has recovered.

---

## 3. Observability & Telemetry

**Metrics (Prometheus/Grafana):**
- **Throughput:** Notifications queued per minute vs. Notifications sent per minute.
- **Latency:** E2E Latency (time from API `202` to Provider `200 OK`).
- **Error Rates:** Grouped by Provider, Tenant, and Channel.
- **Kafka Lag:** Critical metric. If `Consumer Offset < Producer Offset` grows rapidly, workers need to scale out.

**Distributed Tracing (OpenTelemetry):**
- Inject a `trace_id` at the API Gateway.
- Propagate this ID as a Kafka Header into the topics.
- Consumers extract the `trace_id` and include it in logs and outbound HTTP requests.
- This allows full visualization in tools like Jaeger/DataDog from API ingestion to Provider delivery.

**Structured Logging:**
- All logs emitted in JSON format.
- Standard fields: `trace_id`, `tenant_id`, `tracking_id`, `channel`.
