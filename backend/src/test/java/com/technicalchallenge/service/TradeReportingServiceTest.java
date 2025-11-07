package com.technicalchallenge.service;

import com.technicalchallenge.model.Currency;
import com.technicalchallenge.model.Trade;
import com.technicalchallenge.model.TradeLeg;
import com.technicalchallenge.model.TradeStatus;
import com.technicalchallenge.repository.TradeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)

public class TradeReportingServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private TradeReportingService tradeReportingService;

    private List<Trade> trades;
    private String loginId = "joey";


    @Test
    void testGetTradesByTraderReturnsAllTradesForUser() {

        // Given
        trades = List.of(new Trade(),  new Trade());
        when((userDetails.getUsername())).thenReturn(loginId);
        when(tradeRepository.findByTraderAndActiveTrue(loginId)).thenReturn(trades);

        // When
        List<Trade> result = tradeReportingService.getTradesByTrader(userDetails);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(trades.size(),result.size());
        verify(tradeRepository).findByTraderAndActiveTrue(loginId);
    }

    @Test
    void testGetTradesByTraderReturnsNoTradesForUser() {

        // Given
        trades = List.of(new Trade(),  new Trade());
        when((userDetails.getUsername())).thenReturn(loginId);
        when(tradeRepository.findByTraderAndActiveTrue(loginId)).thenReturn(Collections.emptyList());

        // When
        List<Trade> result = tradeReportingService.getTradesByTrader(userDetails);

        // Then
        assertTrue(result.isEmpty());
        verify(tradeRepository).findByTraderAndActiveTrue(loginId);
    }

    @Test
    void testGetTradesByBookIdReturnsMatchingBooks() {

        // Given
        Long bookId = 1000L;
        trades = List.of(new Trade(),  new Trade());
        when(tradeRepository.findByBookIdAndActiveTrue(bookId)).thenReturn(trades);

        // When
        List<Trade> result = tradeReportingService.getTradesByBookId(bookId);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(trades.size(),result.size());
        verify(tradeRepository).findByBookIdAndActiveTrue(bookId);

    }

    @Test
    void testTotalTradesByStatusReturnsGroupedTradeCounts() {

        // Given
        Trade trade1 = new Trade();
        TradeStatus tradeStatus1 = new TradeStatus();
        tradeStatus1.setTradeStatus("LIVE");
        trade1.setTradeStatus(tradeStatus1);

        Trade trade2 = new Trade();
        TradeStatus tradeStatus2 = new TradeStatus();
        tradeStatus2.setTradeStatus("TERMINATED");
        trade2.setTradeStatus(tradeStatus2);

        trades = List.of(trade1,trade2);
        when((userDetails.getUsername())).thenReturn(loginId);
        when(tradeRepository.findByTraderAndActiveTrue(loginId)).thenReturn(trades);

        // When
        Map<String,Long> result = tradeReportingService.totalTradesByStatus(userDetails);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(2,result.size());
        assertEquals(1, result.get("LIVE"));
        assertEquals(1, result.get("TERMINATED"));
        verify(tradeRepository).findByTraderAndActiveTrue(loginId);
    }

    @Test
    void testTotalNotionalByCurrencyReturnsGroupedNotionalAmounts() {

        // Given
        Trade trade1 = new Trade();

        TradeLeg leg1 = new TradeLeg();
        leg1.setNotional(BigDecimal.valueOf(1500000));
        Currency usd = new Currency();
        usd.setCurrency("USD");
        leg1.setCurrency(usd);

        TradeLeg leg2 = new TradeLeg();
        leg2.setNotional(BigDecimal.valueOf(3000000));
        Currency gbp = new Currency();
        gbp.setCurrency("GBP");
        leg2.setCurrency(gbp);

        Trade trade2 = new Trade();

        TradeLeg leg3 = new TradeLeg();
        leg3.setNotional(BigDecimal.valueOf(1000000));
        Currency eur = new Currency();
        eur.setCurrency("EUR");
        leg3.setCurrency(eur);

        TradeLeg leg4 = new TradeLeg();
        leg4.setNotional(BigDecimal.valueOf(2000000));
        leg4.setCurrency(gbp);

        trade1.setTradeLegs(List.of(leg1,leg2));
        trade2.setTradeLegs(List.of(leg3,leg4));

        trades = List.of(trade1,trade2);
        when((userDetails.getUsername())).thenReturn(loginId);
        when(tradeRepository.findByTraderAndActiveTrue(loginId)).thenReturn(trades);

        // When
        Map<String,BigDecimal> result = tradeReportingService.totalNotionalAmountsByCurrency(userDetails);

        // Then
        assertFalse(result.isEmpty());
        assertEquals(BigDecimal.valueOf(5000000), result.get("GBP"));
        assertEquals(BigDecimal.valueOf(1500000), result.get("USD"));
        assertEquals(BigDecimal.valueOf(1000000), result.get("EUR"));
        verify(tradeRepository).findByTraderAndActiveTrue(loginId);
    }

    @Test
    void testTradeCountForTodayReturnsCorrectCount() {

        // Given
        Trade trade1 = new Trade();
        trade1.setTradeDate(LocalDate.now());


        Trade trade2 = new Trade();
        trade2.setTradeDate(LocalDate.now());

        List<Trade> trades = List.of(trade1,trade2);

        when((userDetails.getUsername())).thenReturn(loginId);
        when(tradeRepository.countTradeByTraderAndTradeDate(loginId, LocalDate.now())).thenReturn(2L);

        // When
        long result = tradeReportingService.tradeCountForDate(userDetails, LocalDate.now());

        // Then
        assertTrue(result > 0);
        assertEquals(2, result);
        verify(tradeRepository).countTradeByTraderAndTradeDate(loginId,LocalDate.now());
    }

    @Test
    void testTotalNotionalForTodayReturnsTradesWithCurrentDate() {

        // Given
        Trade trade1 = new Trade();
        trade1.setTradeDate(LocalDate.now());
        TradeLeg leg1 = new TradeLeg();
        leg1.setNotional(BigDecimal.valueOf(1500000));
        TradeLeg leg2 = new TradeLeg();
        leg2.setNotional(BigDecimal.valueOf(3000000));

        Trade trade2 = new Trade();
        trade2.setTradeDate(LocalDate.now());
        TradeLeg leg3 = new TradeLeg();
        leg3.setNotional(BigDecimal.valueOf(1000000));
        TradeLeg leg4 = new TradeLeg();
        leg4.setNotional(BigDecimal.valueOf(2000000));

        trade1.setTradeLegs(List.of(leg1,leg2));
        trade2.setTradeLegs(List.of(leg3,leg4));
        List<Trade> trades = List.of(trade1,trade2);

        when((userDetails.getUsername())).thenReturn(loginId);
        when(tradeRepository.findByTraderAndActiveTrue(loginId)).thenReturn(trades);

        // When
        BigDecimal result = tradeReportingService.notionalAmountForDate(userDetails, LocalDate.now());

        // Then
        assertEquals(BigDecimal.valueOf(7500000), result);
        verify(tradeRepository).findByTraderAndActiveTrue(loginId);
    }
}
