package com.technicalchallenge.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.Map;

@Getter
@Setter
public class TradeSummaryDTO {

    // Total number of trades by status
    Map<String,Long> totalTradesByStatus;

    // Total notional amounts by currency
    Map<String,BigDecimal> totalNotionalByCurrency;

    // Breakdown count of trades by trade type and counterparty
    Map<String, Map<String, Long>> tradesByTypeByCounterparty;

}
