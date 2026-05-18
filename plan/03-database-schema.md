# Notification Service: Database Schema

We recommend using PostgreSQL for strict transactional consistency and JSONB support for flexible metadata storage.

## 1. Tables

### 1.1 `notification_logs`
Stores the immutable history of all notification attempts.

| Column Name | Type | Description |
| :--- | :--- | :--- |
| `id` | UUID (PK) | Primary Identifier |
| `tenant_id` | VARCHAR | Used for multi-tenant isolation |
| `recipient` | VARCHAR | Email address, Phone number, or Device Token |
| `channel` | ENUM | `EMAIL`, `SMS`, `PUSH` |
| `status` | ENUM | `PENDING`, `PROCESSING`, `DELIVERED`, `FAILED` |
| `provider` | VARCHAR | e.g., `SENDGRID`, `TWILIO`, `SES` (recorded post-send) |
| `provider_message_id`| VARCHAR | ID returned by the 3rd party for tracking webhooks |
| `template_id` | UUID (FK) | Reference to the template used |
| `error_details` | TEXT | Reason for failure, if applicable |
| `created_at` | TIMESTAMP | Time of initial request |
| `updated_at` | TIMESTAMP | Last state change |

### 1.2 `templates`
Stores the dynamic content templates.

| Column Name | Type | Description |
| :--- | :--- | :--- |
| `id` | UUID (PK) | Primary Identifier |
| `name` | VARCHAR | Logical name e.g., `welcome_email` |
| `channel` | ENUM | `EMAIL`, `SMS`, `PUSH` |
| `subject_template`| TEXT | e.g., `Welcome to {{appName}}!` |
| `body_template` | TEXT | HTML or plain text with variables |
| `version` | INTEGER | For versioning template updates |
| `is_active` | BOOLEAN | Soft deletion/deactivation flag |

### 1.3 `user_preferences`
Manages global and granular opt-out settings for users to ensure compliance (GDPR, CAN-SPAM).

| Column Name | Type | Description |
| :--- | :--- | :--- |
| `user_id` | VARCHAR (PK) | The internal user identifier |
| `channel` | ENUM (PK) | `EMAIL`, `SMS`, `PUSH` |
| `category` | VARCHAR (PK) | e.g., `MARKETING`, `TRANSACTIONAL`, `ALERTS` |
| `is_opted_in` | BOOLEAN | True if the user wants these notifications |
| `updated_at` | TIMESTAMP | Last modification date |

*Note: Transactional categories often default to TRUE and cannot be modified, whereas Marketing defaults to FALSE depending on region.*

### 1.4 `idempotency_keys`
Prevents duplicate processing of identical requests.

| Column Name | Type | Description |
| :--- | :--- | :--- |
| `key` | VARCHAR (PK) | The provided idempotency key |
| `response_body` | JSONB | The cached `202 Accepted` response |
| `status_code` | INTEGER | E.g., 202 |
| `expires_at` | TIMESTAMP | Used by a cleanup cron job (e.g., TTL 24h) |

---

## 2. Indices

- **`notification_logs`**:
  - Index on `(recipient, created_at)` for fast user-level history lookups.
  - Index on `(provider_message_id)` for fast updates from provider Webhooks.
  - Index on `(status, created_at)` for operational dashboarding.
- **`user_preferences`**:
  - Composite Index on `(user_id, channel, category)` (Covered by PK).
