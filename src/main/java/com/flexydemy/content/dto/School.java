package com.flexydemy.content.dto;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class School {
    private String name;
    private String fromYear;
    private String toYear;
    private String certification;
}
