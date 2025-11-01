package com.technicalchallenge.service;

import com.technicalchallenge.model.Trade;
import com.technicalchallenge.repository.TradeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


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
        logger.debug("Retrieving trades for: {}", userDetails.getUsername());
        return tradeRepository.findByTrader(userDetails.getUsername());
    }

    // For Book-level trade aggregation
    public List<Trade> getTradesByBookId(Long bookId) {
        logger.debug("Retrieving trades for book with ID: {}", bookId);
        return tradeRepository.findByBookId(bookId);
    }

}
