package com.flexydemy.content.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class MonthlyEarningDTO {
    private int index;
    private String month;
    private BigDecimal sessionEarnings;
    private BigDecimal payoutEarnings;
    private BigDecimal total;

    public MonthlyEarningDTO(int index, String month, BigDecimal sessionEarnings, BigDecimal payoutEarnings) {
        this.index = index;
        this.month = month;
        this.sessionEarnings = sessionEarnings;
        this.payoutEarnings = payoutEarnings;
        this.total = sessionEarnings.add(payoutEarnings);
    }
}