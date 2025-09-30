package com.flexydemy.content.dto;

import lombok.Data;

import java.util.List;

@Data
public class AssessmentsDTO {
    private String title;
    private String type;
    private int questions;
    private int timeLimit;
    private List<String> subject;
    private double averageScore;
    private String status;
    private int attempts;
}
