package com.flexydemy.content.dto;

import com.flexydemy.content.enums.Class_Categories;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class CourseDTO {
    private String courseId;
    private String courseTitle;
    private String description;
    private String studentId;
    private String tutorId;
    private Class_Categories subjectCategory;
    private String gradeLevel;
    private boolean isPublished;
    private String duration;
    private int ratingsCount;
    private double averageRating;
    private int lessonCount;
    private int enrollments;
    private LocalDate uploadedDate;
}
