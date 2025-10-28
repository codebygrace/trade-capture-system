package com.technicalchallenge.controller;

import com.technicalchallenge.dto.TradeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
Properties for the application are overridden by src/test/resources/application.properties
These integration tests use a separate H2 in-memory database to ensure isolation
Test data is in src/test/resources/data.sql to reduce repetition
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TradeControllerIT {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost:";

    private static TestRestTemplate restTemplate;

    @BeforeEach
    public void setUp() {
        restTemplate = new TestRestTemplate();
        baseUrl = baseUrl + port + "/api/trades";
    }

    // This helper method has been added to reduce repetition
    private List<TradeDTO> searchTrades(String searchCriteria) {
        ResponseEntity<List<TradeDTO>> response = restTemplate.exchange(
                baseUrl+"/search?" + searchCriteria,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
        assertEquals(HttpStatus.OK,response.getStatusCode());
        return response.getBody();
    }

    @Test
    @DisplayName("Search for book name 'FX-BOOK-1' returns a list of trades")
    void testSearchByBookNameInDBReturnsResult() {
        List<TradeDTO> trades = searchTrades("bookName=FX-BOOK-1");
        assertEquals("FX-BOOK-1", trades.get(0).getBookName());
    }

    @Test
    @DisplayName("Search for book name 'TEST-BOOK' not in DB returns an empty")
    void testSearchByBookNameNotInDBReturnsEmptyList() {
        List<TradeDTO> trades = searchTrades("bookName=TEST-BOOK");
        assertTrue(trades.isEmpty());
    }

    @Test
    @DisplayName("Search for counterparty 'BigBank' returns a list of trades")
    void testSearchByCounterpartyNameInDBReturnsResult() {
        List<TradeDTO> trades = searchTrades("counterpartyName=BigBank");
        assertEquals("BigBank", trades.get(0).getCounterpartyName());
    }

    @Test
    @DisplayName("Search for counterparty 'TestBank' not in DB returns empty list")
    void testSearchByCounterpartyNameNotInDBReturnsEmptyList() {

        List<TradeDTO> trades = searchTrades("counterpartyName=TestBank");
        assertTrue(trades.isEmpty());
    }

    @Test
    @DisplayName("Search for trader 'Simon' returns a list of trades")
    void testSearchByTraderNameInDBReturnsResult() {
        List<TradeDTO> trades = searchTrades("trader=Simon");
        assertEquals("Simon King", trades.get(0).getTraderUserName());
    }

    @Test
    @DisplayName("Search for trader 'Test' not in DB returns empty list")
    void testSearchByTraderNameNotInDBReturnsEmptyList() {

        List<TradeDTO> trades = searchTrades("trader=Test");
        assertTrue(trades.isEmpty());
    }

    @Test
    @DisplayName("Search for trade with status 'LIVE' returns a list of trades")
    void testSearchByTradeStatusInDBReturnsResult() {
        List<TradeDTO> trades = searchTrades("status=LIVE");
        assertEquals("LIVE", trades.get(0).getTradeStatus());
    }

    @Test
    @DisplayName("Search for trade status 'TEST' not in DB returns empty list")
    void testSearchByTradeStatusNotInDBReturnsEmptyList() {

        List<TradeDTO> trades = searchTrades("status=TEST");
        assertTrue(trades.isEmpty());
    }

    @Test
    @DisplayName("Search for trades with tradeDate >= 2024-06-01 should return a list containing trades")
    void testSearchTradeDateStartInDBReturnsResult() {
        List<TradeDTO> trades = searchTrades("tradeDateStart=2024-06-01");

        LocalDate tradeDate = trades.get(0).getTradeDate();

        assertFalse(tradeDate.isBefore(LocalDate.of(2024, 6, 1)));
    }

    @Test
    @DisplayName("Search for trades with tradeDate >= 2029-10-01 should return an empty list")
    void testSearchByTradeDateStartNotInDBReturnsEmptyList() {
        List<TradeDTO> trades = searchTrades("tradeDateStart=2029-10-01");
        assertTrue(trades.isEmpty());
    }

    @Test
    @DisplayName("Search for trades where 2024-06-01 <= tradeDate <= 2029-10-01 should return list containing trades")
    void testSearchByTradeDateStartAndTradeDateEndReturnsList() {
        List<TradeDTO> trades = searchTrades("tradeDateStart=2024-06-01&tradeDateEnd=2029-10-01");

        LocalDate tradeDate = trades.get(0).getTradeDate();

        assertTrue(!tradeDate.isBefore(LocalDate.of(2024,6,1)) &&
                            !tradeDate.isAfter(LocalDate.of(2024,6,1)));
    }

    @Test
    @DisplayName("Search for trade by book name, counterparty, trader and tradeDateStart should return a list")
    void testSearchByBookNameAndCounterpartyAndTradeDateStartInDBReturnsResult() {
        List<TradeDTO> trades = searchTrades("bookName=FX-BOOK-1&counterpartyName=BigBank&trader=Simon&tradeDateStart=2024-06-01");

        LocalDate tradeDate = trades.get(0).getTradeDate();

        assertEquals("FX-BOOK-1", trades.get(0).getBookName());
        assertEquals("BigBank", trades.get(0).getCounterpartyName());
        assertEquals("Simon King", trades.get(0).getTraderUserName());
        assertFalse(tradeDate.isBefore(LocalDate.of(2024, 6, 1)));
    }
}
