package com.flexydemy.content.dto;

import jakarta.persistence.Embeddable;
import lombok.Data;

import java.time.LocalDate;

@Data
@Embeddable
public class WorkExperience {
    private String companyName;
    private String jobTitle;
    private String responsibilities;
    private LocalDate fromYear;
    private LocalDate toYear;
}
