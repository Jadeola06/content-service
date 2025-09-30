package com.flexydemy.content.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "google_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleCredential {

    @Id
    private String userId; // e.g. "user"

    @Column(columnDefinition = "TEXT")
    private String accessToken;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;

    private Long expirationTimeMilliseconds;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
