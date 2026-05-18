package com.example.notification.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "idempotency_keys")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyKey {

    @Id
    @Column(name = "key", length = 255)
    private String key;

    @Column(name = "response_body", columnDefinition = "JSONB")
    private String responseBody;

    @Column(name = "status_code")
    private Integer statusCode;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
}
