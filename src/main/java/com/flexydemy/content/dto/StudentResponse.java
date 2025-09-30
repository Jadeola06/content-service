package com.flexydemy.content.dto;

import lombok.Data;

@Data
public class StudentResponse {
    private String id;
    private String name;
    private int age;
    private String subject;
    private String exam;
    private boolean active;
    private String profileImageUrl;
    private double completionRate;
}

