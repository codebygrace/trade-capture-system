package com.technicalchallenge.controller;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
Properties for the application are overridden by src/test/resources/application.properties
These integration tests use a separate H2 in-memory database to ensure isolation
Test data is in src/test/resources/data.sql to reduce repetition
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class TradeControllerValidationIT {

    @LocalServerPort
    private int port;

    private String baseUrl = "http://localhost:";

    private static TestRestTemplate restTemplate;

    @BeforeEach
    public void setUp() {

        /* This constructs a TestRestTemplate that automatically includes HTTP Basic authentication credentials
            for a user in the database */
        restTemplate = new TestRestTemplate("simon", "password");
        baseUrl = baseUrl + port + "/api/trades";
    }

    @Test
    @DisplayName("Create trade with with maturity before trade date and start date returns 400 error")
    void testInvalidMaturityDateReturnsErrors() {

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(10000000.0));
        leg1.setRate(0.5);
        leg1.setLegType("Fixed");
        leg1.setPayReceiveFlag("Pay");

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(10000000.0));
        leg2.setIndexName("LIBOR");
        leg2.setLegType("Floating");
        leg2.setPayReceiveFlag("Receive");

        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().minusYears(1));
        tradeDTO.setTradeLegs(List.of(leg1, leg2));
        tradeDTO.setCounterpartyName("BigBank");
        tradeDTO.setBookName("FX-BOOK-1");
        tradeDTO.setTraderUserName("simon");
        tradeDTO.setTraderUserId(1003L);
        tradeDTO.setInputterUserName("simon");
        tradeDTO.setTradeInputterUserId(1003L);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(tradeDTO),
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Maturity date cannot be before start date"));
        assertTrue(response.getBody().contains("Maturity date cannot be before trade date"));

    }

    @Test
    @DisplayName("Create trade with with start date before trade date returns 400 error")
    void testStartBeforeTradeDateReturnsErrors() {

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(10000000.0));
        leg1.setRate(0.5);
        leg1.setLegType("Fixed");
        leg1.setPayReceiveFlag("Pay");

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(10000000.0));
        leg2.setIndexName("LIBOR");
        leg2.setLegType("Floating");
        leg2.setPayReceiveFlag("Receive");

        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().minusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(leg1, leg2));
        tradeDTO.setCounterpartyName("BigBank");
        tradeDTO.setBookName("FX-BOOK-1");
        tradeDTO.setTraderUserName("simon");
        tradeDTO.setTraderUserId(1003L);
        tradeDTO.setInputterUserName("simon");
        tradeDTO.setTradeInputterUserId(1003L);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(tradeDTO),
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Start date cannot be before trade date"));
    }

    @Test
    @DisplayName("Trade date 31 days in past returns an error")
    void testTradeDate31DaysInPastReturnsErrors() {

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(10000000.0));
        leg1.setRate(0.5);
        leg1.setLegType("Fixed");
        leg1.setPayReceiveFlag("Pay");

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(10000000.0));
        leg2.setIndexName("LIBOR");
        leg2.setLegType("Floating");
        leg2.setPayReceiveFlag("Receive");

        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now().minusDays(31));
        tradeDTO.setTradeStartDate(LocalDate.now());
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(leg1, leg2));
        tradeDTO.setCounterpartyName("BigBank");
        tradeDTO.setBookName("FX-BOOK-1");
        tradeDTO.setTraderUserName("simon");
        tradeDTO.setTraderUserId(1003L);
        tradeDTO.setInputterUserName("simon");
        tradeDTO.setTradeInputterUserId(1003L);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(tradeDTO),
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Trade date cannot be more than 30 days in the past"));
    }

    @Test
    @DisplayName("Trade with null legs returns an error")
    void testTradeNullLegsReturnsErrors() {

        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now().minusDays(31));
        tradeDTO.setTradeStartDate(LocalDate.now());
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(null);
        tradeDTO.setCounterpartyName("BigBank");
        tradeDTO.setBookName("FX-BOOK-1");
        tradeDTO.setTraderUserName("simon");
        tradeDTO.setTraderUserId(1003L);
        tradeDTO.setInputterUserName("simon");
        tradeDTO.setTradeInputterUserId(1003L);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(tradeDTO),
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Trade legs must have exactly 2 legs"));
    }

    @Test
    @DisplayName("Trade with 1 leg returns an error")
    void testTrade1LegReturnsErrors() {

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(10000000.0));
        leg1.setRate(0.5);
        leg1.setLegType("Fixed");
        leg1.setPayReceiveFlag("Pay");

        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now().minusDays(31));
        tradeDTO.setTradeStartDate(LocalDate.now());
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(leg1));
        tradeDTO.setCounterpartyName("BigBank");
        tradeDTO.setBookName("FX-BOOK-1");
        tradeDTO.setTraderUserName("simon");
        tradeDTO.setTraderUserId(1003L);
        tradeDTO.setInputterUserName("simon");
        tradeDTO.setTradeInputterUserId(1003L);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(tradeDTO),
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Trade legs must have exactly 2 legs"));
    }

    @Test
    @DisplayName("Inactive reference data returns an errors")
    void testInactiveReferenceDataReturnsErrors() {

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(10000000.0));
        leg1.setRate(0.5);
        leg1.setLegType("Fixed");
        leg1.setPayReceiveFlag("Pay");

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(10000000.0));
        leg2.setIndexName("LIBOR");
        leg2.setLegType("Floating");
        leg2.setPayReceiveFlag("Receive");

        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(leg1, leg2));
        tradeDTO.setCounterpartyName("InactiveBank");
        tradeDTO.setBookName("INACTIVE-BOOK-1");
        tradeDTO.setTraderUserName("simon");
        tradeDTO.setTraderUserId(1003L);
        tradeDTO.setInputterUserName("simon");
        tradeDTO.setTradeInputterUserId(1003L);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(tradeDTO),
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Book must be active"));
        assertTrue(response.getBody().contains("Counterparty must be active"));
    }

    @Test
    @DisplayName("TRADER_SALES amending trade for another trader returns 403 error")
    void testInvalidTraderSalesAmendOperationReturnsError() {

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(10000000.0));
        leg1.setRate(0.5);
        leg1.setLegType("Fixed");
        leg1.setPayReceiveFlag("Pay");

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(10000000.0));
        leg2.setIndexName("LIBOR");
        leg2.setLegType("Floating");
        leg2.setPayReceiveFlag("Receive");

        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setId(100001L);
        tradeDTO.setTradeId(100001L);
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now());
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(leg1, leg2));
        tradeDTO.setCounterpartyName("BigBank");
        tradeDTO.setBookName("FX-BOOK-1");
        tradeDTO.setTraderUserName("joey");
        tradeDTO.setTraderUserId(1005L);
        tradeDTO.setInputterUserName("simon");
        tradeDTO.setTradeInputterUserId(1003L);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/100001",
                HttpMethod.PUT,
                new HttpEntity<>(tradeDTO),
                String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("This account lacks the required privileges for this operation"));
    }

    @Test
    @DisplayName("MO amending trade returns OK")
    void testMOAmendOperationOK() {

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(10000000.0));
        leg1.setRate(0.5);
        leg1.setLegType("Fixed");
        leg1.setPayReceiveFlag("Pay");

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(10000000.0));
        leg2.setIndexName("LIBOR");
        leg2.setLegType("Floating");
        leg2.setPayReceiveFlag("Receive");

        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setId(100001L);
        tradeDTO.setTradeId(100001L);
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now());
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(leg1, leg2));
        tradeDTO.setCounterpartyName("BigBank");
        tradeDTO.setBookName("FX-BOOK-1");
        tradeDTO.setTraderUserName("joey");
        tradeDTO.setTraderUserId(1005L);
        tradeDTO.setInputterUserName("ashley");
        tradeDTO.setTradeInputterUserId(1004L);

        TestRestTemplate moRestTemplate = new TestRestTemplate("ashley","password");

        ResponseEntity<String> response = moRestTemplate.exchange(
                baseUrl + "/100001",
                HttpMethod.PUT,
                new HttpEntity<>(tradeDTO),
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("MO creating trade returns 403 error")
    void testMOCreateOperationError() {

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(10000000.0));
        leg1.setRate(0.5);
        leg1.setLegType("Fixed");
        leg1.setPayReceiveFlag("Pay");

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(10000000.0));
        leg2.setIndexName("LIBOR");
        leg2.setLegType("Floating");
        leg2.setPayReceiveFlag("Receive");

        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now());
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(leg1, leg2));
        tradeDTO.setCounterpartyName("BigBank");
        tradeDTO.setBookName("FX-BOOK-1");
        tradeDTO.setTraderUserName("joey");
        tradeDTO.setTraderUserId(1005L);
        tradeDTO.setInputterUserName("ashley");
        tradeDTO.setTradeInputterUserId(1004L);

        TestRestTemplate moRestTemplate = new TestRestTemplate("ashley","password");

        ResponseEntity<String> response = moRestTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                new HttpEntity<>(tradeDTO),
                String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Forbidden"));
    }

    @Test
    @DisplayName("SUPPORT attempting to cancel trade returns 403 error")
    void testInvalidSupportAmendOperationReturnsError() {

        TestRestTemplate supportRestTemplate = new TestRestTemplate("alice","password");

        ResponseEntity<String> response = supportRestTemplate.exchange(
                baseUrl + "/100001",
                HttpMethod.DELETE,
                null,
                String.class
        );
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Forbidden"));
    }

    @Test
    @DisplayName("SUPERUSER attempting to cancel trade returns 200 OK")
    void testSuperuserCancelTradeReturnsOK() {

        TestRestTemplate superuserRestTemplate = new TestRestTemplate("stuart","password");

        ResponseEntity<String> response = superuserRestTemplate.exchange(
                baseUrl + "/100001",
                HttpMethod.DELETE,
                null,
                String.class
        );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("Trade cancelled successfully"));
    }
}
