package com.flexydemy.content.dto;

import com.flexydemy.content.enums.NotificationTypes;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class NotificationResponse {
    private String id;
    private String userId;
    private String content;
    private LocalDateTime time;
    private NotificationTypes type;
}
