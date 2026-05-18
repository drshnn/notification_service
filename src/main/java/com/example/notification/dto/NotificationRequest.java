package com.example.notification.dto;

import com.example.notification.domain.enums.Channel;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotNull(message = "Channel is required")
    private Channel channel;

    @NotBlank(message = "Category is required")
    private String category;

    @NotBlank(message = "Recipient is required")
    private String recipient;

    @NotBlank(message = "Template name is required")
    private String templateName;

    private Map<String, Object> templateVariables;

    private LocalDateTime scheduledFor;
}
