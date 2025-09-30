package com.flexydemy.content.dto;

import com.flexydemy.content.enums.Class_Categories;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CourseDisplayDTO {
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

    private LocalDate uploadedDate;

    private List<LessonIntroDTO> lessons;
    private List<RatingDTO> ratings;
    private int studentCount;
    private int lessonCount;

    private String tutor;
    private String lastUpdated;
}
