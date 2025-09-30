package com.flexydemy.content.dto;

import lombok.Data;

@Data
public class ContinueLearning {
    private String courseId;
    private String courseName;
    private double percentage;
    private String nextLesson;
}
