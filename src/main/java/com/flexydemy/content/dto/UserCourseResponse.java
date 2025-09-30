package com.flexydemy.content.dto;

import com.flexydemy.content.enums.Class_Categories;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class UserCourseResponse {
    private String courseId;
    private String courseTitle;
    private String description;
    private String studentId;
    private String feedback;
    private String tutorId;
    private Class_Categories subjectCategory;
    private String gradeLevel;
    private boolean isPublished;
    private BigDecimal price;
    private String duration;
    private int ratingsCount;
    private double averageRating;

    private double progressPercentage;

    private int lessonCount;
}
