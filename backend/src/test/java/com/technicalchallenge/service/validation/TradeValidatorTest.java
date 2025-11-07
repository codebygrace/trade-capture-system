package com.technicalchallenge.service.validation;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TradeValidatorTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @Mock
    private TradeLegValidator tradeLegValidator;

    private TradeValidator tradeValidator;
    private Book book;
    private Counterparty counterparty;

    @BeforeEach
    public void setUp() {

        tradeValidator = new TradeValidator(bookRepository, counterpartyRepository, tradeLegValidator);
        book = new Book();
        counterparty = new Counterparty();
    }

    @Test
    @DisplayName("Valid trade returns no errors")
    void testValidTradeReturnsNoErrors() {

        // Given
        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(new TradeLegDTO(),  new TradeLegDTO()));
        tradeDTO.setCounterpartyName("Test_counterparty");
        tradeDTO.setBookName("Test_book");

        book.setActive(true);
        counterparty.setActive(true);

        ValidationResult legResult = new ValidationResult();

        when(tradeLegValidator.validateTradeLegConsistency(tradeDTO.getTradeLegs())).thenReturn(legResult);
        when(bookRepository.findByBookName("Test_book")).thenReturn(Optional.of(book));
        when(counterpartyRepository.findByName("Test_counterparty")).thenReturn(Optional.of(counterparty));

        // When
        ValidationResult result = tradeValidator.validateTradeBusinessRules(tradeDTO);

        // Then
        assertTrue(result.isValid());

    }

    @Test
    @DisplayName("Maturity date before trade date and start date returns errors")
    void testInvalidMaturityDateReturnsErrors() {

        // Given
        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().minusYears(1));
        tradeDTO.setTradeLegs(List.of(new TradeLegDTO(),  new TradeLegDTO()));
        tradeDTO.setCounterpartyName("Test_counterparty");
        tradeDTO.setBookName("Test_book");

        book.setActive(true);
        counterparty.setActive(true);

        ValidationResult legResult = new ValidationResult();

        when(tradeLegValidator.validateTradeLegConsistency(tradeDTO.getTradeLegs())).thenReturn(legResult);
        when(bookRepository.findByBookName("Test_book")).thenReturn(Optional.of(book));
        when(counterpartyRepository.findByName("Test_counterparty")).thenReturn(Optional.of(counterparty));

        // When
        ValidationResult result = tradeValidator.validateTradeBusinessRules(tradeDTO);

        // Then
        assertFalse(result.isValid());
        assertEquals(2,result.getErrors().size());
        assertTrue(result.getErrors().containsEntry("tradeMaturityDate", "Maturity date cannot be before start date"));
        assertTrue(result.getErrors().containsEntry("tradeMaturityDate", "Maturity date cannot be before trade date"));
    }

    @Test
    @DisplayName("Start date before trade date returns an error")
    void testInvalidStarDateDateReturnsError() {

        // Given
        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().minusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(new TradeLegDTO(),  new TradeLegDTO()));
        tradeDTO.setCounterpartyName("Test_counterparty");
        tradeDTO.setBookName("Test_book");

        book.setActive(true);
        counterparty.setActive(true);

        ValidationResult legResult = new ValidationResult();

        when(tradeLegValidator.validateTradeLegConsistency(tradeDTO.getTradeLegs())).thenReturn(legResult);
        when(bookRepository.findByBookName("Test_book")).thenReturn(Optional.of(book));
        when(counterpartyRepository.findByName("Test_counterparty")).thenReturn(Optional.of(counterparty));

        // When
        ValidationResult result = tradeValidator.validateTradeBusinessRules(tradeDTO);

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsEntry("tradeStartDate", "Start date cannot be before trade date"));
    }

    @Test
    @DisplayName("Trade date 60 days in past returns an error")
    void testTradeDate60DaysInPastReturnsError() {

        // Given
        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now().minusDays(60));
        tradeDTO.setTradeStartDate(LocalDate.now());
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(new TradeLegDTO(),  new TradeLegDTO()));
        tradeDTO.setCounterpartyName("Test_counterparty");
        tradeDTO.setBookName("Test_book");

        book.setActive(true);
        counterparty.setActive(true);

        ValidationResult legResult = new ValidationResult();

        when(tradeLegValidator.validateTradeLegConsistency(tradeDTO.getTradeLegs())).thenReturn(legResult);
        when(bookRepository.findByBookName("Test_book")).thenReturn(Optional.of(book));
        when(counterpartyRepository.findByName("Test_counterparty")).thenReturn(Optional.of(counterparty));

        // When
        ValidationResult result = tradeValidator.validateTradeBusinessRules(tradeDTO);

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsEntry("tradeDate", "Trade date cannot be more than 30 days in the past"));
    }

    @Test
    @DisplayName("Trade null legs returns an error")
    void testTradeWithNullLegsReturnsError() {

        // Given
        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(null);
        tradeDTO.setCounterpartyName("Test_counterparty");
        tradeDTO.setBookName("Test_book");

        book.setActive(true);
        counterparty.setActive(true);

        when(bookRepository.findByBookName("Test_book")).thenReturn(Optional.of(book));
        when(counterpartyRepository.findByName("Test_counterparty")).thenReturn(Optional.of(counterparty));

        // When
        ValidationResult result = tradeValidator.validateTradeBusinessRules(tradeDTO);

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsEntry("tradeLegs", "Trade legs must have exactly 2 legs"));
    }

    @Test
    @DisplayName("Trade with 1 leg returns an error")
    void testTradeWith1LegReturnsError() {

        // Given
        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(new TradeLegDTO()));
        tradeDTO.setCounterpartyName("Test_counterparty");
        tradeDTO.setBookName("Test_book");

        book.setActive(true);
        counterparty.setActive(true);

        when(bookRepository.findByBookName("Test_book")).thenReturn(Optional.of(book));
        when(counterpartyRepository.findByName("Test_counterparty")).thenReturn(Optional.of(counterparty));

        // When
        ValidationResult result = tradeValidator.validateTradeBusinessRules(tradeDTO);

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsEntry("tradeLegs", "Trade legs must have exactly 2 legs"));
    }

    @Test
    @DisplayName("Trade with inactive book returns an error")
    void testTradeWithInactiveBookReturnsError() {

        // Given
        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(new TradeLegDTO(),  new TradeLegDTO()));
        tradeDTO.setCounterpartyName("Test_counterparty");
        tradeDTO.setBookName("Test_book");

        book.setActive(false);
        counterparty.setActive(true);

        ValidationResult legResult = new ValidationResult();

        when(tradeLegValidator.validateTradeLegConsistency(tradeDTO.getTradeLegs())).thenReturn(legResult);
        when(bookRepository.findByBookName("Test_book")).thenReturn(Optional.of(book));
        when(counterpartyRepository.findByName("Test_counterparty")).thenReturn(Optional.of(counterparty));

        // When
        ValidationResult result = tradeValidator.validateTradeBusinessRules(tradeDTO);

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsEntry("book", "Book must be active"));
    }

    @Test
    @DisplayName("Trade with inactive counterparty returns an error")
    void testTradeWithInactiveCounterpartyReturnsError() {

        // Given
        TradeDTO tradeDTO = new TradeDTO();
        tradeDTO.setTradeDate(LocalDate.now());
        tradeDTO.setTradeStartDate(LocalDate.now().plusDays(2));
        tradeDTO.setTradeMaturityDate(LocalDate.now().plusYears(1));
        tradeDTO.setTradeLegs(List.of(new TradeLegDTO(),  new TradeLegDTO()));
        tradeDTO.setCounterpartyName("Test_counterparty");
        tradeDTO.setBookName("Test_book");

        book.setActive(true);
        counterparty.setActive(false);

        ValidationResult legResult = new ValidationResult();

        when(tradeLegValidator.validateTradeLegConsistency(tradeDTO.getTradeLegs())).thenReturn(legResult);
        when(bookRepository.findByBookName("Test_book")).thenReturn(Optional.of(book));
        when(counterpartyRepository.findByName("Test_counterparty")).thenReturn(Optional.of(counterparty));

        // When
        ValidationResult result = tradeValidator.validateTradeBusinessRules(tradeDTO);

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsEntry("counterparty", "Counterparty must be active"));
    }
}
