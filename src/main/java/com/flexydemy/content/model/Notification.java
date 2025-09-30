package com.flexydemy.content.model;

import com.flexydemy.content.enums.NotificationTypes;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String userId; // the recipient

    @Enumerated(EnumType.STRING)
    private NotificationTypes type;

    private String title;

    @Column(length = 1000)
    private String content;

    private String senderId; // e.g. related message/thread/content ID
    private String relatedEntityId;

    private boolean read = false;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }





}
