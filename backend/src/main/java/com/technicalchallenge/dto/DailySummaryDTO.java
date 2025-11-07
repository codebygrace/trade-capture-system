package com.technicalchallenge.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DailySummaryDTO {

    // Today's trade count
    long tradeCountToday;

    // Today's notional
    BigDecimal notionalAmountToday;

    // For comparison with previous trading day
    long tradeCountYesterday;

    // For comparison with previous trading day
    BigDecimal notionalAmountYesterday;

}
