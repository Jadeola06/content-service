package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LearningTimeByMonthDTO {
    private String month;
    private int monthIndex;
    private double morningHours;
    private double afternoonHours;
    private double eveningHours;
}
