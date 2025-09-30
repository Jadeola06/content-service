package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@NoArgsConstructor
@AllArgsConstructor
@Data
public class MessageListDisplayDTO {
    private String userId;
    private String name;
    private LocalDateTime time;
    private boolean isUserActive;
    private boolean isRead;
    private String profileImageUrl;
}

