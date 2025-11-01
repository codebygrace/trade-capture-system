package com.technicalchallenge.service;

import com.technicalchallenge.model.*;
import com.technicalchallenge.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
public class TradeReportingService {

    private static final Logger logger = LoggerFactory.getLogger(TradeReportingService.class);

    private final TradeRepository tradeRepository;

    public TradeReportingService(TradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    // Find all trades for a logged in trader
    public List<Trade> getTradesByTrader(UserDetails userDetails) {
        logger.info("Retrieving trades for: {}", userDetails.getUsername());
        return tradeRepository.findByTrader(userDetails.getUsername());
    }

    // For Book-level trade aggregation
    public List<Trade> getTradesByBookId(Long bookId) {
        logger.info("Retrieving trades for book with ID: {}", bookId);
        return tradeRepository.findByBookId(bookId);
    }

    // Total number of trades by status
    public Map<String, Long> totalTradesByStatus(UserDetails userDetails) {
        logger.info("Counting trades by trade status for: {}", userDetails.getUsername());
        List<Trade> userTrades = tradeRepository.findByTrader(userDetails.getUsername());
        return userTrades.stream()
                .collect(Collectors.groupingBy(trade -> trade.getTradeStatus().getTradeStatus(), Collectors.counting()));
    }

    // Total notional amounts by currency
    public Map<String, BigDecimal> totalNotionalAmountsByCurrency(UserDetails userDetails) {
        logger.info("Retrieving total notional amounts by currency for: {} ", userDetails.getUsername());
        List<Trade> userTrades = tradeRepository.findByTrader(userDetails.getUsername());
        return userTrades.stream().flatMap( trade -> trade.getTradeLegs().stream())
                .collect(Collectors.groupingBy(leg -> leg.getCurrency().getCurrency(),
                        Collectors.reducing(BigDecimal.ZERO,TradeLeg::getNotional, BigDecimal::add)));
    }

    // Breakdown count of trades by trade type and counterparty
    public Map<String, Map<String, Long>> totalTradesByTradeTypeAndCounterparty(UserDetails userDetails) {
        List<Trade> userTrades = tradeRepository.findByTrader(userDetails.getUsername());
        return userTrades.stream()
                .collect(Collectors.groupingBy(trade -> trade.getTradeType().getTradeType(),
                        Collectors.groupingBy(trade -> trade.getCounterparty().getName(), Collectors.counting())));
    }

}
