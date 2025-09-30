package com.flexydemy.content.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class Auditable {

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt; // Automatically set when the entity is created

    @Column(name = "last_login")
    private LocalDateTime lastLogin; // Store the last login time

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt; // Automatically set when the entity is updated

    @Column(name = "modified_by")
    private String modifiedBy; // Store the user who modified the entity

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate; // Store the date/time when the entity was modified
}
