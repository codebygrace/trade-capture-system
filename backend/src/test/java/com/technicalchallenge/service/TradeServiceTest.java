package com.technicalchallenge.service;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.dto.TradeFilterDTO;
import com.technicalchallenge.dto.TradeLegDTO;
import com.technicalchallenge.model.*;
import com.technicalchallenge.repository.*;
import com.technicalchallenge.service.validation.UserPrivilegeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeServiceTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeLegRepository tradeLegRepository;

    @Mock
    private CashflowRepository cashflowRepository;

    @Mock
    private TradeStatusRepository tradeStatusRepository;

    @Mock
    private AdditionalInfoService additionalInfoService;

    @Mock
    BookRepository bookRepository;

    @Mock
    CounterpartyRepository counterpartyRepository;

    @Mock
    Specification<Trade> specification;

    @Mock
    UserPrivilegeValidator userPrivilegeValidator;

    @InjectMocks
    private TradeService tradeService;

    private TradeDTO tradeDTO;
    private Trade trade;

    @BeforeEach
    void setUp() {
        // Set up test data
        tradeDTO = new TradeDTO();
        tradeDTO.setTradeId(100001L);
        tradeDTO.setTradeDate(LocalDate.of(2025, 1, 15));
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 17));
        tradeDTO.setTradeMaturityDate(LocalDate.of(2026, 1, 17));

        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setNotional(BigDecimal.valueOf(1000000));
        leg1.setRate(0.05);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setNotional(BigDecimal.valueOf(1000000));
        leg2.setRate(0.0);

        tradeDTO.setTradeLegs(Arrays.asList(leg1, leg2));

        trade = new Trade();
        trade.setId(1L);
        trade.setTradeId(100001L);
    }

    @Test
    void testCreateTrade_Success() {
        // Given
        TradeStatus tradeStatus = new TradeStatus();
        tradeStatus.setTradeStatus("NEW");

        tradeDTO.setBookName("Test Book");
        tradeDTO.setCounterpartyName("Test Counterparty");
        tradeDTO.setTradeStatus("NEW");

        when(bookRepository.findByBookName(anyString())).thenReturn(Optional.of(new Book()));
        when(counterpartyRepository.findByName(anyString())).thenReturn(Optional.of(new Counterparty()));
        when(tradeStatusRepository.findByTradeStatus(anyString())).thenReturn(Optional.of(tradeStatus));
        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(new TradeLeg());
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);

        // When
        Trade result = tradeService.createTrade(tradeDTO);

        // Then
        assertNotNull(result);
        assertEquals(100001L, result.getTradeId());
        verify(tradeRepository).save(any(Trade.class));
    }

    @Test
    void testCreateTrade_InvalidDates_ShouldFail() {
        // Given - This test is intentionally failing for candidates to fix
        tradeDTO.setTradeStartDate(LocalDate.of(2025, 1, 10)); // Before trade date

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        // This assertion is intentionally wrong - candidates need to fix it
        assertEquals("Start date cannot be before trade date", exception.getMessage());
    }

    @Test
    void testCreateTrade_InvalidLegCount_ShouldFail() {
        // Given
        tradeDTO.setTradeLegs(Arrays.asList(new TradeLegDTO())); // Only 1 leg

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.createTrade(tradeDTO);
        });

        assertTrue(exception.getMessage().contains("exactly 2 legs"));
    }

    @Test
    void testGetTradeById_Found() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));

        // When
        Optional<Trade> result = tradeService.getTradeById(100001L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(100001L, result.get().getTradeId());
    }

    @Test
    void testGetTradeById_NotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When
        Optional<Trade> result = tradeService.getTradeById(999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testAmendTrade_Success() {
        // Given
        trade.setVersion(1);

        when(tradeLegRepository.save(any(TradeLeg.class))).thenReturn(new TradeLeg());
        when(tradeRepository.findByTradeIdAndActiveTrue(100001L)).thenReturn(Optional.of(trade));
        when(tradeStatusRepository.findByTradeStatus("AMENDED")).thenReturn(Optional.of(new com.technicalchallenge.model.TradeStatus()));
        when(tradeRepository.save(any(Trade.class))).thenReturn(trade);
        when(userPrivilegeValidator.validateUserPrivileges(any(), eq("AMEND"), any(TradeDTO.class))).thenReturn(true);

        // When
        Trade result = tradeService.amendTrade(100001L, tradeDTO);

        // Then
        assertNotNull(result);
        verify(tradeRepository, times(2)).save(any(Trade.class)); // Save old and new
    }

    @Test
    void testAmendTrade_TradeNotFound() {
        // Given
        when(tradeRepository.findByTradeIdAndActiveTrue(999L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.amendTrade(999L, tradeDTO);
        });

        assertTrue(exception.getMessage().contains("Trade not found"));
    }

    // This test has a deliberate bug for candidates to find and fix
    @Test
    void testCashflowGeneration_MonthlySchedule() throws Exception {
        // This test method is incomplete and has logical errors
        // Candidates need to implement proper cashflow testing

        // Given
        Schedule monthlySchedule = new Schedule();
        monthlySchedule.setSchedule("1M"); // creates monthly schedule

        TradeLeg leg = new TradeLeg();
        leg.setNotional(BigDecimal.valueOf(1000000));
        leg.setCalculationPeriodSchedule(monthlySchedule);  // assigns schedule to the leg

        Method generateCashflowsMethod = TradeService.class.getDeclaredMethod("generateCashflows", TradeLeg.class, LocalDate.class,LocalDate.class); // accesses the private method
        generateCashflowsMethod.setAccessible(true); // allows access to the private method by temporarily bypassing access rules

        // When
        generateCashflowsMethod.invoke(tradeService,leg,tradeDTO.getTradeStartDate(),tradeDTO.getTradeMaturityDate());

        // Then
        verify(cashflowRepository, times(12)).save(any(Cashflow.class)); // checks that 12 cashflows are saved to cashflowRepository for each month from 2025-01-17 to 2026-01-17
    }

    @Test
    public void testGetTradesByMultiCriteria_searchByCounterparty_returnsList() {

        // Given
        Counterparty counterparty = new Counterparty();
        counterparty.setName("BigBank");

        trade.setCounterparty(counterparty);

        when(tradeRepository.findByMultiCriteria("BigBank",null,null,null,null,null)).thenReturn(List.of(trade));

        // When
        List<Trade> result = tradeService.getTradesByMultiCriteria("BigBank",null,null,null,null,null);

        // Then
        assertNotNull(result);
        assertTrue(result.contains(trade));
        verify(tradeRepository).findByMultiCriteria("BigBank",null,null,null,null,null);
    }

    @Test
    public void testGetTradesByMultiCriteria_nullSearch_returnsAll() {

        // Given
        List<Trade> trades = List.of(trade);
        when(tradeRepository.findByMultiCriteria(null,null,null,null,null,null)).thenReturn(trades);

        // When
        List<Trade> result = tradeService.getTradesByMultiCriteria(null,null,null,null,null,null);

        // Then
        assertNotNull(result);
        assertTrue(result.contains(trade));
        verify(tradeRepository).findByMultiCriteria( null,null,null,null,null,null);
    }

    @Test
    public void testGetTradeByFilter_Pagination() {

        // Given
        TradeFilterDTO tradeFilterDTO = new TradeFilterDTO();
        tradeFilterDTO.setCounterpartyName("BigBank");

        Pageable pageable = PageRequest.of(0,10);

        Page<Trade> resultPage = new PageImpl<>(List.of(trade),pageable,1);

        when(tradeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(resultPage);

        // When
        Page<Trade> result = tradeService.getAllTradesByFilter(tradeFilterDTO,pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().contains(trade));
        assertEquals(0, result.getNumber());
        assertEquals(10, result.getSize());
    }

    @Test
    public void testGetTradeByFilter_nullFilter_returnsPage() {

        // Given
        TradeFilterDTO tradeFilterDTO = null;

        Pageable pageable = PageRequest.of(0,10);

        Page<Trade> resultPage = new PageImpl<>(List.of(trade),pageable,List.of(trade).size());

        when(tradeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(resultPage);

        // When
        Page<Trade> result = tradeService.getAllTradesByFilter(tradeFilterDTO,pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().contains(trade));
        assertEquals(1,result.getTotalElements());
        verify(tradeRepository).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    public void testGetTradesByRsqlQuery_stringQuery_returnsResultsPage() {

        // Given
        trade.setTradeDate(LocalDate.of(2025, 1, 15));

        String query = "tradeDate=ge=2025-01-01";
        Pageable pageable = PageRequest.of(0,10);
        Page<Trade> resultPage = new PageImpl<>(List.of(trade),pageable,List.of(trade).size());

        when(tradeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(resultPage);

        // When
        Page<Trade> result = tradeService.getTradesByRsqlQuery(query,pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().contains(trade));
        assertEquals(1,result.getTotalElements());
        verify(tradeRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    public void testGetTradesByRsqlQuery_nullQuery_returnsResultsPage() {

        // Given
        String query = null;
        Pageable pageable = PageRequest.of(0,10);
        Page<Trade> resultPage = new PageImpl<>(List.of(trade),pageable,List.of(trade).size());

        when(tradeRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(resultPage);

        // When
        Page<Trade> result = tradeService.getTradesByRsqlQuery(query,pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().contains(trade));
        assertEquals(1,result.getTotalElements());
        verify(tradeRepository).findAll(any(Specification.class), eq(pageable));
    }
}
