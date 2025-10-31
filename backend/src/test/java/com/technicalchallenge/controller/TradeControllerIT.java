package com.technicalchallenge.controller;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.technicalchallenge.dto.TradeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

        /* This constructs a TestRestTemplate that automatically includes HTTP Basic authentication credentials
            for a user in the database */
        restTemplate = new TestRestTemplate("simon", "password");
        baseUrl = baseUrl + port + "/api/trades";

        // verify test data is loaded correctly as it uses data.sql
        verifyTestDataExists();
    }

    // Check to see test data required for tests is present
    private void verifyTestDataExists() {
        ResponseEntity<List<TradeDTO>> response = restTemplate.exchange(
                baseUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isEmpty());

        List<TradeDTO> trades = response.getBody();

        // Check that expected test data exist in data.sql
        assertTrue(trades.stream().anyMatch(trade -> "FX-BOOK-1".equals(trade.getBookName())),
                "Test data should contain trade associated with FX-BOOK-1");

        assertTrue(trades.stream().anyMatch(trade -> "BigBank".equals(trade.getCounterpartyName())),
                "Test data should contain trade associated with BigBank");

        assertTrue(trades.stream().anyMatch(trade -> "Simon King".equals(trade.getTraderUserName())),
                "Test data should contain trade associated with trader Simon King");

        assertTrue(trades.stream().anyMatch(trade -> "LIVE".equals(trade.getTradeStatus())),
                "Test data should contain trade with LIVE status");

        assertTrue(trades.stream().anyMatch( trade -> !trade.getTradeDate().isBefore(LocalDate.of(2024,6,1)) &&
                !trade.getTradeDate().isAfter(LocalDate.of(2024,6,30))),
                "Test data should contain trade with trade date in June 2024");
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
        assertEquals("FX-BOOK-1", trades.getFirst().getBookName());
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
        assertEquals("BigBank", trades.getFirst().getCounterpartyName());
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
        assertEquals("Simon King", trades.getFirst().getTraderUserName());
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
        assertEquals("LIVE", trades.getFirst().getTradeStatus());
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

        LocalDate tradeDate = trades.getFirst().getTradeDate();

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

        LocalDate tradeDate = trades.getFirst().getTradeDate();

        assertTrue(!tradeDate.isBefore(LocalDate.of(2024,6,1)) &&
                            !tradeDate.isAfter(LocalDate.of(2029,10,1)));
    }

    @Test
    @DisplayName("Search for trade by book name, counterparty, trader and tradeDateStart should return a list")
    void testSearchByBookNameAndCounterpartyAndTradeDateStartInDBReturnsResult() {
        List<TradeDTO> trades = searchTrades("bookName=FX-BOOK-1&counterpartyName=BigBank&trader=Simon&tradeDateStart=2024-06-01");

        LocalDate tradeDate = trades.getFirst().getTradeDate();

        assertEquals("FX-BOOK-1", trades.getFirst().getBookName());
        assertEquals("BigBank", trades.getFirst().getCounterpartyName());
        assertEquals("Simon King", trades.getFirst().getTraderUserName());
        assertFalse(tradeDate.isBefore(LocalDate.of(2024, 6, 1)));
    }

    @Test
    @DisplayName("Search for trades with empty string search criteria should return all trades")
    void testSearchByNullReturnsAll() {
        List<TradeDTO> trades = searchTrades("");

        assertFalse(trades.isEmpty());
        assertEquals(2, trades.size());
    }

    @Test
    @DisplayName("Search for trade by invalid date should return 400 and error message")
    void testSearchByInvalidDateShouldReturn400() {

        String searchCriteria = "tradeDateStart=204-06-02";

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl+"/search?" + searchCriteria,
                HttpMethod.GET,
                null,
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        assertNotNull(response.getBody());
    }

    // Helper method for testing paging
    @JsonIgnoreProperties(ignoreUnknown = true, value = "pageable")
    public static class RestResponsePage<T> extends PageImpl<T> {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public RestResponsePage(@JsonProperty("content") List<T> content,
                                @JsonProperty("number") int number,
                                @JsonProperty("size") int size,
                                @JsonProperty("totalElements") long total) {
            super(content, PageRequest.of(number,size), total);
        }
    }

    private RestResponsePage<TradeDTO> filterTrades(String filterCriteria) {

        ParameterizedTypeReference<RestResponsePage<TradeDTO>> responseType =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<RestResponsePage<TradeDTO>> response = restTemplate.exchange(
                baseUrl+"/filter?" + filterCriteria,
                HttpMethod.GET,
                null,
                responseType
        );
        assertEquals(HttpStatus.OK,response.getStatusCode());
        return response.getBody();
    }

    @Test
    @DisplayName("Filter for book name 'FX-BOOK-1' returns a Page of trades")
    void testFilterByBookNameInDBReturnsResult() {
        RestResponsePage<TradeDTO> trades = filterTrades("bookName=FX-BOOK-1");
        assertEquals("FX-BOOK-1", trades.getContent().getFirst().getBookName());
    }

    @Test
    @DisplayName("Filter for book name 'TEST-BOOK' not in DB returns an empty")
    void testFilterByBookNameNotInDBReturnsEmptyPage() {
        Page<TradeDTO> trades = filterTrades("bookName=TEST-BOOK");
        assertTrue(trades.isEmpty());
    }

    @Test
    @DisplayName("Filter for counterparty 'BigBank' returns a Page of trades")
    void testFilterByCounterpartyNameInDBReturnsResult() {
        Page<TradeDTO> trades = filterTrades("counterpartyName=BigBank");
        assertEquals("BigBank", trades.getContent().getFirst().getCounterpartyName());
    }

    @Test
    @DisplayName("Filter for counterparty 'TestBank' not in DB returns empty Page")
    void testFilterByCounterpartyNameNotInDBReturnsEmptyPage() {

        Page<TradeDTO> trades = filterTrades("counterpartyName=TestBank");
        assertTrue(trades.isEmpty());
    }

    @Test
    @DisplayName("Filter for trader 'Simon' returns a Page of trades")
    void testFilterByTraderNameInDBReturnsResult() {
        Page<TradeDTO> trades = filterTrades("trader=Simon");
        assertEquals("Simon King", trades.getContent().getFirst().getTraderUserName());
    }

    @Test
    @DisplayName("Filter for trader 'Test' not in DB returns empty Page")
    void testFilterByTraderNameNotInDBReturnsEmptyPage() {

        Page<TradeDTO> trades = filterTrades("trader=Test");
        assertTrue(trades.isEmpty());
    }

    @Test
    @DisplayName("Filter for trade with status 'LIVE' returns a Page of trades")
    void testFilterByTradeStatusInDBReturnsResult() {
        Page<TradeDTO> trades = filterTrades("status=LIVE");
        assertEquals("LIVE", trades.getContent().getFirst().getTradeStatus());
    }

    @Test
    @DisplayName("Filter for trade status 'TEST' not in DB returns empty Page")
    void testFilterByTradeStatusNotInDBReturnsEmptyPage() {

        Page<TradeDTO> trades = filterTrades("status=TEST");
        assertTrue(trades.isEmpty());
    }

    @Test
    @DisplayName("Filter for trades with tradeDate >= 2024-06-01 should return a Page containing trades")
    void testFilterTradeDateStartInDBReturnsResult() {
        Page<TradeDTO> trades = filterTrades("tradeDateStart=2024-06-01");

        LocalDate tradeDate = trades.getContent().getFirst().getTradeDate();

        assertFalse(tradeDate.isBefore(LocalDate.of(2024, 6, 1)));
    }

    @Test
    @DisplayName("Filter for trades where 2024-06-01 <= tradeDate <= 2029-10-01 should return Page containing trades")
    void testFilterByTradeDateStartAndTradeDateEndReturnsPage() {
        Page<TradeDTO> trades = filterTrades("tradeDateStart=2024-06-01&tradeDateEnd=2029-10-01");

        LocalDate tradeDate = trades.getContent().getFirst().getTradeDate();

        assertTrue(!tradeDate.isBefore(LocalDate.of(2024,6,1)) &&
                !tradeDate.isAfter(LocalDate.of(2029,10,1)));
    }

    @Test
    @DisplayName("Filter for trade by book name, counterparty, trader and tradeDateStart should return a Page of trades")
    void testFilterByBookNameAndCounterpartyAndTradeDateStartInDBReturnsResult() {
        Page<TradeDTO> trades = filterTrades("bookName=FX-BOOK-1&counterpartyName=BigBank&trader=Simon&tradeDateStart=2024-06-01");

        LocalDate tradeDate = trades.getContent().getFirst().getTradeDate();

        assertEquals("FX-BOOK-1", trades.getContent().getFirst().getBookName());
        assertEquals("BigBank", trades.getContent().getFirst().getCounterpartyName());
        assertEquals("Simon King", trades.getContent().getFirst().getTraderUserName());
        assertFalse(tradeDate.isBefore(LocalDate.of(2024, 6, 1)));
    }

    @Test
    @DisplayName("Filter for trades with empty filter criteria should return all trades")
    void testFilterByNullReturnsAll() {
        Page<TradeDTO> trades = filterTrades("");

        assertFalse(trades.isEmpty());
        assertEquals(2, trades.getContent().size());
    }

    @Test
    @DisplayName("Filter for trades by invalid date should return 400 and error message")
    void testFilterByInvalidDateShouldReturn400() {

        String filterCriteria = "tradeDateStart=204-06-02";

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl+"/filter?" + filterCriteria,
                HttpMethod.GET,
                null,
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        assertNotNull(response.getBody());
    }

    private RestResponsePage<TradeDTO> rsqlQueryTrades(String query) {

        ParameterizedTypeReference<RestResponsePage<TradeDTO>> responseType =
                new ParameterizedTypeReference<>() {};
        ResponseEntity<RestResponsePage<TradeDTO>> response = restTemplate.exchange(
                baseUrl+"/rsql?" + query,
                HttpMethod.GET,
                null,
                responseType
        );
        assertEquals(HttpStatus.OK,response.getStatusCode());
        return response.getBody();
    }

    @Test
    @DisplayName("Query by book name 'FX-BOOK-1' returns a Page of trades")
    void testQueryByBookNameInDBReturnsResult() {
        RestResponsePage<TradeDTO> trades = rsqlQueryTrades("book.bookName==FX-BOOK-1");
        assertEquals("FX-BOOK-1", trades.getContent().getFirst().getBookName());
    }

    @Test
    @DisplayName("Query by counterparty name 'BigBank' returns a Page of trades")
    void testQueryByCounterpartyNameInDBReturnsResult() {
        RestResponsePage<TradeDTO> trades = rsqlQueryTrades("counterparty.name==BigBank");
        assertEquals("BigBank", trades.getContent().getFirst().getCounterpartyName());
    }

    @Test
    @DisplayName("Query by trader 'simon' returns a Page of trades")
    void testQueryByTraderInDBReturnsResult() {
        RestResponsePage<TradeDTO> trades = rsqlQueryTrades("trader==simon");
        assertEquals("Simon King", trades.getContent().getFirst().getTraderUserName());
    }

    @Test
    @DisplayName("Query by status 'LIVE' returns a Page of trades")
    void testQueryByStatusInDBReturnsResult() {
        RestResponsePage<TradeDTO> trades = rsqlQueryTrades("tradeStatus.tradeStatus==LIVE");
        assertEquals("LIVE", trades.getContent().getFirst().getTradeStatus());
    }

    @Test
    @DisplayName("Query trades by counterparty names and status 'LIVE' should return a Page of trades")
    void testQueryByCounterpartyNamesAndStatusInDBReturnsResult() {
        Page<TradeDTO> trades = rsqlQueryTrades("(counterparty.name==BigBank,counterparty.name==MegaFund);tradeStatus.tradeStatus==LIVE");

        assertTrue(trades.getContent().getFirst().getBookName().equals("FX-BOOK-1") || trades.getContent().getFirst().getCounterpartyName().equals("MegaFund"));
        assertEquals("LIVE", trades.getContent().getFirst().getTradeStatus());

    }

    @Test
    @DisplayName("Query by date range should return a Page of trades")
    void testQueryByDateRangeInDBReturnsResult() {
        Page<TradeDTO> trades = rsqlQueryTrades("tradeDate=ge=2024-06-01;tradeDate=le=2024-06-30");

        LocalDate tradeDate = trades.getContent().getFirst().getTradeDate();

        assertTrue(!tradeDate.isBefore(LocalDate.of(2024,6,1)) &&
                        !tradeDate.isAfter(LocalDate.of(2024,6,30)));
    }

    @Test
    @DisplayName("RSQL query with invalid date should return 400 and error message")
    void testQueryByInvalidDateShouldReturn400() {

        String query = "tradeDate=e=224-55-01";

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl+"/rsql" + query,
                HttpMethod.GET,
                null,
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("RSQL query with invalid field 'countparty' should return 400 and error message")
    void testQueryByInvalidFieldShouldReturn400() {

        String query = "countparty.name==MegaFund";

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl+"/rsql" + query,
                HttpMethod.GET,
                null,
                String.class
        );
        assertEquals(HttpStatus.BAD_REQUEST,response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
