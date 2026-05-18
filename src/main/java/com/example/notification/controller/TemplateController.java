package com.example.notification.controller;

import com.example.notification.domain.entity.Template;
import com.example.notification.domain.enums.Channel;
import com.example.notification.dto.TemplateRequest;
import com.example.notification.dto.TemplateResponse;
import com.example.notification.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @PostMapping
    public ResponseEntity<TemplateResponse> createTemplate(@Valid @RequestBody TemplateRequest request) {
        Template template = templateService.createTemplate(
                request.getName(),
                request.getChannel(),
                request.getSubjectTemplate(),
                request.getBodyTemplate()
        );
        return new ResponseEntity<>(mapToResponse(template), HttpStatus.CREATED);
    }

    @GetMapping("/{name}/{channel}")
    public ResponseEntity<TemplateResponse> getTemplate(
            @PathVariable String name,
            @PathVariable Channel channel) {
        Template template = templateService.getTemplate(name, channel);
        return ResponseEntity.ok(mapToResponse(template));
    }

    private TemplateResponse mapToResponse(Template template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .channel(template.getChannel())
                .subjectTemplate(template.getSubjectTemplate())
                .bodyTemplate(template.getBodyTemplate())
                .version(template.getVersion())
                .isActive(template.getIsActive())
                .build();
    }
}
