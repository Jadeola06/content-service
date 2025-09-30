package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageThreadDTO {
    private String threadId;
    private String receiverId;
    private String receiverFirstName;
    private String receiverLastName;
    private String receiverProfileImageUrl;
    private LocalDateTime lastMessageTime;
    private boolean isRead;
    private List<MessageDTO> messages;
}
