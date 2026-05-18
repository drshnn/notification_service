package com.example.notification.domain.entity;

import com.example.notification.domain.enums.Channel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserPreference.UserPreferenceId.class)
public class UserPreference {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPreferenceId implements Serializable {
        private String userId;
        private Channel channel;
        private String category;
    }

    @Id
    @Column(name = "user_id")
    private String userId;

    @Id
    @Enumerated(EnumType.STRING)
    private Channel channel;

    @Id
    private String category;

    @Column(name = "is_opted_in", nullable = false)
    @Builder.Default
    private Boolean isOptedIn = true;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
