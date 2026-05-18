# Notification Service: API Contracts

The API acts as the front door for all notification requests. It is designed to be fully asynchronous to protect the callers from high latency.

## 1. POST /api/v1/notifications
Submits a new notification request. The server validates the payload and queues it.

**Headers:**
- `Idempotency-Key` (Optional, String): A unique UUID provided by the client. Recommended for retries on network failures.
- `Authorization`: Bearer `<token>`
- `X-Tenant-Id`: (Optional) String representing the organization.

**Request Body (JSON):**
```json
{
  "channel": "EMAIL", // Required: EMAIL, SMS, PUSH
  "category": "TRANSACTIONAL", // Required: Maps to User Preferences
  "recipient": "user@example.com", // Required: Target address
  "template_name": "welcome_email", // Required: Name of template to render
  "template_variables": { // Optional: Map of key/values
    "appName": "Acme Corp",
    "userFirstName": "Alice",
    "actionUrl": "https://acme.com/start"
  },
  "scheduled_for": "2024-12-31T23:59:00Z" // Optional: Future delivery
}
```

**Response: `202 Accepted`**
```json
{
  "tracking_id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "status": "PENDING",
  "message": "Notification queued for processing."
}
```

**Response: `409 Conflict`** (If Idempotency-Key matches an existing processed request)
Returns the original `202 Accepted` response.

---

## 2. GET /api/v1/notifications/{tracking_id}/status
Retrieves the real-time status of a queued notification.

**Response: `200 OK`**
```json
{
  "tracking_id": "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
  "status": "DELIVERED",
  "channel": "EMAIL",
  "provider": "SENDGRID",
  "created_at": "2024-10-27T10:00:00Z",
  "updated_at": "2024-10-27T10:00:05Z",
  "error_message": null
}
```

---

## 3. Webhooks (Inbound)

Providers like SendGrid or Twilio will send asynchronous updates regarding message delivery, bounces, or spam reports.

**POST /api/v1/webhooks/sendgrid**
- Accepts raw payload from SendGrid.
- Validates the signature (X-Twilio-Email-Event-Webhook-Signature).
- Extracts the `provider_message_id` and publishes an event to `notification.status.events` to be reconciled with our internal `tracking_id`.
