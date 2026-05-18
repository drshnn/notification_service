package com.example.notification.domain.entity;

import com.example.notification.domain.enums.Channel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Column(name = "subject_template", columnDefinition = "TEXT")
    private String subjectTemplate;

    @Column(name = "body_template", columnDefinition = "TEXT", nullable = false)
    private String bodyTemplate;

    @Builder.Default
    private Integer version = 1;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
