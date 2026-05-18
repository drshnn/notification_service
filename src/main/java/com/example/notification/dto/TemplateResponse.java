package com.example.notification.dto;

import com.example.notification.domain.enums.Channel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateResponse {
    private UUID id;
    private String name;
    private Channel channel;
    private String subjectTemplate;
    private String bodyTemplate;
    private Integer version;
    private Boolean isActive;
}
