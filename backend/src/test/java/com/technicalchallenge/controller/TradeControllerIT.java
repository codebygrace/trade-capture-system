package com.technicalchallenge.controller;

import com.technicalchallenge.dto.TradeDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TradeControllerIT {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost:";

    private static TestRestTemplate restTemplate;

    @BeforeAll
    public static void init() {
        restTemplate = new TestRestTemplate();
    }

    @BeforeEach
    public void setUp() {
        baseUrl = baseUrl + port + "/api/trades";
    }

    @Test
    void testSearchByBookNameInDBReturnsResult() {

        ResponseEntity<List<TradeDTO>> response = restTemplate.exchange(
                baseUrl+"/search?bookName=FX-BOOK-1",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        List<TradeDTO> trades = response.getBody();

        assertFalse(trades.isEmpty());
        assertTrue(trades.get(0).getBookName().equals("FX-BOOK-1"));
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testSearchByBookNameNotInDBReturnsEmptyList() {
        ResponseEntity<List<TradeDTO>> response = restTemplate.exchange(
                baseUrl+"/search?bookName=TEST-BOOK",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        List<TradeDTO> trades = response.getBody();

        assertTrue(trades.isEmpty());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testSearchByCounterpartyNameInDBReturnsResult() {

        ResponseEntity<List<TradeDTO>> response = restTemplate.exchange(
                baseUrl+"/search?counterpartyName=BigBank",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        List<TradeDTO> trades = response.getBody();

        assertFalse(trades.isEmpty());
        assertTrue(trades.get(0).getCounterpartyName().equals("BigBank"));
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testSearchByCounterpartyNameNotInDBReturnsEmptyList() {

        ResponseEntity<List<TradeDTO>> response = restTemplate.exchange(
                baseUrl+"/search?counterpartyName=TestCounterparty",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        List<TradeDTO> trades = response.getBody();

        assertTrue(trades.isEmpty());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void testSearchByTradeDateStartNotInDBReturnsEmptyList() {

        ResponseEntity<List<TradeDTO>> response = restTemplate.exchange(
                baseUrl+"/search?tradeDateStart=2029-10-01",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                });

        List<TradeDTO> trades = response.getBody();

        assertTrue(trades.isEmpty());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }
}
