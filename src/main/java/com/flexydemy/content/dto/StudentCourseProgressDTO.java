package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseProgressDTO {
    private String userId;
    private String username;
    private String courseId;
    private String courseTitle;
    private int currentLessonSequence;
    private double progressPercentage;
}
