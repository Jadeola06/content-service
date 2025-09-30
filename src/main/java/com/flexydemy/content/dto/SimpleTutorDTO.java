package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleTutorDTO {
    private String tutorId;
    private String name;
    private String profileImageUrl;
    private double rating;
    private LocalDateTime nextAvailableTime;
    private String subject;
    private String experience;
}
