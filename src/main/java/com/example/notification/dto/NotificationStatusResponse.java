package com.example.notification.dto;

import com.example.notification.domain.enums.Channel;
import com.example.notification.domain.enums.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationStatusResponse {
    private UUID trackingId;
    private NotificationStatus status;
    private Channel channel;
    private String provider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String errorMessage;
}
