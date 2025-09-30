package com.flexydemy.content.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminMonthlyEarningBreakdown {
    private String month;
    private BigDecimal jambEarnings;
    private BigDecimal waecEarnings;
}
