package com.flexydemy.content.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopCourseDTO {
    private String courseId;
    private String courseName;
    private String category;
    private int studentCount;
    private double rating;
    private BigDecimal earnings;
}