package com.technicalchallenge.dto;

import java.math.BigDecimal;

public class DailySummaryDTO {

    // Today's trade count
    Long tradeCountToday;

    // Today's notional
    BigDecimal notionalAmountToday;

    // For comparison with previous trading day
    Long tradeCountYesterday;

    // For comparison with previous trading day
    BigDecimal notionalAmountYesterday;

}
