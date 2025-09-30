package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RatingDTO {
    private String userId;
    private String tutorId;
    private String username;
    private String courseId;
    private int rating;
    private String feedback;
    private LocalDateTime time;

    public RatingDTO(String studentId, String studentUsername, int rating, String feedback, LocalDateTime time) {
    }
}
