package com.example.notification.service;

import com.example.notification.domain.entity.Template;
import com.example.notification.domain.enums.Channel;
import com.example.notification.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final TemplateRepository templateRepository;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)}}");

    public String resolveTemplate(String templateName, Channel channel, Map<String, Object> variables) {
        Template template = templateRepository.findByNameAndChannel(templateName, channel)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + templateName + " for channel " + channel));

        return replaceVariables(template.getBodyTemplate(), variables);
    }

    private String replaceVariables(String body, Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            return body;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(body);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String variableName = matcher.group(1).trim();
            Object value = variables.get(variableName);
            String replacement = value != null ? value.toString() : "";
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public Template createTemplate(String name, Channel channel, String subjectTemplate, String bodyTemplate) {
        Template template = Template.builder()
                .name(name)
                .channel(channel)
                .subjectTemplate(subjectTemplate)
                .bodyTemplate(bodyTemplate)
                .version(1)
                .isActive(true)
                .build();
        return templateRepository.save(template);
    }

    public Template getTemplate(String name, Channel channel) {
        return templateRepository.findByNameAndChannel(name, channel)
                .orElseThrow(() -> new IllegalArgumentException("Template not found: " + name + " for channel " + channel));
    }
}
