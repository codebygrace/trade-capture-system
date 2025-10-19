package com.technicalchallenge.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class TradeSummaryDTO {

    // Total number of trades by status
    Map<String,Integer> totalTradesByStatus;

    // Total notional amounts by currency
    Map<String,BigDecimal> totalNotionalByCurrency;

    // Breakdown by trade type and counterparty
    Map<String, String> tradesByTypeByCounterparty;

    // Risk exposure summaries //TODO - confirm what the requirement is
}
