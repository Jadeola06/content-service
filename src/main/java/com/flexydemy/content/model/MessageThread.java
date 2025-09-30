package com.flexydemy.content.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageThread {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String senderId;
    private String receiverId;

    private String threadKey;

    private String senderFirstName;
    private String senderLastName;

    private String receiverFirstName;
    private String receiverLastName;

    private String senderProfileImageUrl;
    private String receiverProfileImageUrl;

    private LocalDateTime lastMessageAt;

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Message> messages;

    private boolean isRead = false;

    @PrePersist
    public void prePersist() {
        this.lastMessageAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.lastMessageAt = LocalDateTime.now();
    }


}
