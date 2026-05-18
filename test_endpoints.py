import requests
import time
import uuid

BASE_URL = "http://localhost:8080/api/v1"

def test_endpoints():
    print("🚀 Starting Notification Service E2E Tests...\n")
    
    # 1. Create a Template
    print("1️⃣ Testing: POST /templates (Creating a new template)")
    template_payload = {
        "name": "welcome_email",
        "channel": "EMAIL",
        "subjectTemplate": "Welcome to {{appName}}!",
        "bodyTemplate": "Hi {{userFirstName}}, thanks for joining {{appName}}. Here is your link: {{actionUrl}}"
    }
    
    try:
        response = requests.post(f"{BASE_URL}/templates", json=template_payload)
        response.raise_for_status()
        print(f"✅ Success! Created template. Response:\n{response.json()}\n")
    except requests.exceptions.RequestException as e:
        print(f"❌ Failed to create template: {e}")
        if e.response is not None:
            print(e.response.text)
        return

    # 2. Get the Template
    print("2️⃣ Testing: GET /templates/{name}/{channel} (Fetching the template)")
    try:
        response = requests.get(f"{BASE_URL}/templates/welcome_email/EMAIL")
        response.raise_for_status()
        print(f"✅ Success! Fetched template. Response:\n{response.json()}\n")
    except requests.exceptions.RequestException as e:
        print(f"❌ Failed to fetch template: {e}")
        return

    # 3. Send a Notification
    print("3️⃣ Testing: POST /notifications (Queueing a notification)")
    idempotency_key = str(uuid.uuid4())
    notification_payload = {
        "channel": "EMAIL",
        "category": "TRANSACTIONAL",
        "recipient": "testuser@example.com",
        "templateName": "welcome_email",
        "templateVariables": {
            "appName": "Acme Corp",
            "userFirstName": "Alice",
            "actionUrl": "https://acme.com/start"
        }
    }
    
    headers = {
        "X-Tenant-Id": "tenant-123",
        "Idempotency-Key": idempotency_key
    }
    
    try:
        response = requests.post(f"{BASE_URL}/notifications", json=notification_payload, headers=headers)
        response.raise_for_status()
        res_data = response.json()
        tracking_id = res_data.get("trackingId")
        print(f"✅ Success! Notification queued. Tracking ID: {tracking_id}\n")
    except requests.exceptions.RequestException as e:
        print(f"❌ Failed to send notification: {e}")
        if e.response is not None:
            print(e.response.text)
        return

    # 4. Test Idempotency
    print("4️⃣ Testing: POST /notifications (Idempotency Check - using same key)")
    try:
        response = requests.post(f"{BASE_URL}/notifications", json=notification_payload, headers=headers)
        response.raise_for_status()
        print(f"✅ Success! Received cached idempotency response:\n{response.json()}\n")
    except requests.exceptions.RequestException as e:
        print(f"❌ Failed idempotency check: {e}")
        return

    # 5. Check Status (Wait a second for Kafka worker to process)
    print("⏳ Waiting 2 seconds for Kafka worker to process the notification...")
    time.sleep(2)
    
    print("5️⃣ Testing: GET /notifications/{trackingId}/status (Checking delivery status)")
    try:
        response = requests.get(f"{BASE_URL}/notifications/{tracking_id}/status")
        response.raise_for_status()
        print(f"✅ Success! Current Status:\n{response.json()}\n")
    except requests.exceptions.RequestException as e:
        print(f"❌ Failed to get status: {e}")
        return

    print("🎉 All endpoints tested successfully!")

if __name__ == "__main__":
    test_endpoints()
