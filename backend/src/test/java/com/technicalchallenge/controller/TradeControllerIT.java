package com.technicalchallenge.controller;

import com.technicalchallenge.dto.TradeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    void testSearchByBookNameInDBReturnsResult() {
        List<TradeDTO> trades = searchTrades("bookName=FX-BOOK-1");
        assertEquals("FX-BOOK-1", trades.get(0).getBookName());
    }

    @Test
    void testSearchByBookNameNotInDBReturnsEmptyList() {
        List<TradeDTO> trades = searchTrades("bookName=TEST-BOOK");
        assertTrue(trades.isEmpty());
    }

    @Test
    void testSearchByCounterpartyNameInDBReturnsResult() {
        List<TradeDTO> trades = searchTrades("counterpartyName=BigBank");
        assertEquals("BigBank", trades.get(0).getCounterpartyName());
    }

    @Test
    void testSearchByCounterpartyNameNotInDBReturnsEmptyList() {

        List<TradeDTO> trades = searchTrades("counterpartyName=TestBank");
        assertTrue(trades.isEmpty());
    }

    @Test
    void testSearchByTradeDateStartNotInDBReturnsEmptyList() {
        List<TradeDTO> trades = searchTrades("tradeDateStart=2029-10-01");
        assertTrue(trades.isEmpty());
    }
}
