package com.flexydemy.content.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TutorEarningsDTO {
    private BigDecimal sessionEarnings;
    private BigDecimal payoutEarnings;
    private BigDecimal totalEarnings;
}