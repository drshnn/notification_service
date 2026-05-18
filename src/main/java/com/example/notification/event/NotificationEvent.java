package com.example.notification.event;

import com.example.notification.domain.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private UUID trackingId;
    private Channel channel;
    private String recipient;
    private String category;
    private String templateName;
    private Map<String, Object> templateVariables;
    private String tenantId;
}
