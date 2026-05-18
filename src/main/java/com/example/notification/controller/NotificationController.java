package com.example.notification.controller;

import com.example.notification.dto.NotificationRequest;
import com.example.notification.dto.NotificationResponse;
import com.example.notification.dto.NotificationStatusResponse;
import com.example.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> submitNotification(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader(value = "X-Tenant-Id", required = false) String tenantId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        
        NotificationResponse response = notificationService.processNotification(request, tenantId, idempotencyKey);
        return ResponseEntity.accepted().body(response);
    }

    @GetMapping("/{trackingId}/status")
    public ResponseEntity<NotificationStatusResponse> getNotificationStatus(
            @PathVariable UUID trackingId) {
        
        NotificationStatusResponse response = notificationService.getStatus(trackingId);
        return ResponseEntity.ok(response);
    }
}
