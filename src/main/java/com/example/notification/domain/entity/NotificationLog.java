package com.example.notification.domain.entity;

import com.example.notification.domain.enums.Channel;
import com.example.notification.domain.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notification_logs", indexes = {
        @Index(name = "idx_recipient_created", columnList = "recipient, created_at"),
        @Index(name = "idx_provider_msg_id", columnList = "provider_message_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(nullable = false)
    private String recipient;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    private String provider;

    @Column(name = "provider_message_id")
    private String providerMessageId;

    @Column(name = "template_id")
    private UUID templateId;

    @Column(name = "error_details", columnDefinition = "TEXT")
    private String errorDetails;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
