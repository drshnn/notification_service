package com.example.notification.dto;

import com.example.notification.domain.enums.Channel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateRequest {
    
    @NotBlank(message = "Template name is required")
    private String name;

    @NotNull(message = "Channel is required")
    private Channel channel;

    private String subjectTemplate;

    @NotBlank(message = "Body template is required")
    private String bodyTemplate;
}
