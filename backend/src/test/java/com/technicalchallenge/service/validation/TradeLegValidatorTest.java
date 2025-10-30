package com.technicalchallenge.service.validation;

import com.technicalchallenge.dto.TradeLegDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TradeLegValidatorTest {

    TradeLegValidator tradeLegValidator = new TradeLegValidator();

    @Test
    @DisplayName("Legs with opposite pay/receive flags return no errors")
    public void testLegsWithOppositePayRecFlagsReturnsNoErrors() {

        // Given
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setPayReceiveFlag("pay");
        leg1.setLegType("FIXED");
        leg1.setRate(5.0);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setPayReceiveFlag("receive");
        leg2.setLegType("FLOATING");
        leg2.setIndexName("LIBOR");

        List<TradeLegDTO> tradeLegDTOs = List.of(leg1,leg2);

        // When
        ValidationResult result = tradeLegValidator.validateTradeLegConsistency(tradeLegDTOs);

        // Then
        assertTrue(result.isValid());
        assertEquals(0,result.getErrors().size());
    }

    @Test
    @DisplayName("Both legs with 'pay' returns an error")
    public void testLegsWithSamePayRecFlagsReturnsErrors() {

        // Given
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setPayReceiveFlag("pay");
        leg1.setLegType("FIXED");
        leg1.setRate(5.0);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setPayReceiveFlag("pay");
        leg2.setLegType("FLOATING");
        leg2.setIndexName("LIBOR");

        List<TradeLegDTO> tradeLegDTOs = List.of(leg1,leg2);

        // When
        ValidationResult result = tradeLegValidator.validateTradeLegConsistency(tradeLegDTOs);

        System.out.println("Validation errors: " + result.getErrors());

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsValue("Legs must have opposite pay/receive flags"));
    }

    @Test
    @DisplayName("Floating leg without an index returns an error")
    public void testFloatingLegWithoutAnIndexReturnsAnError() {

        // Given
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setPayReceiveFlag("pay");
        leg1.setLegType("FIXED");
        leg1.setRate(5.0);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setPayReceiveFlag("receive");
        leg2.setLegType("FLOATING");
        leg2.setIndexName(null);

        List<TradeLegDTO> tradeLegDTOs = List.of(leg1,leg2);

        // When
        ValidationResult result = tradeLegValidator.validateTradeLegConsistency(tradeLegDTOs);

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsValue("Floating legs must have an index specified"));
    }

    @Test
    @DisplayName("Floating leg with empty string index returns an error")
    public void testFloatingLegWithEmptyStringIndexNameReturnsAnError() {

        // Given
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setPayReceiveFlag("pay");
        leg1.setLegType("FIXED");
        leg1.setRate(5.0);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setPayReceiveFlag("receive");
        leg2.setLegType("FLOATING");
        leg2.setIndexName("");

        List<TradeLegDTO> tradeLegDTOs = List.of(leg1,leg2);

        // When
        ValidationResult result = tradeLegValidator.validateTradeLegConsistency(tradeLegDTOs);

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsValue("Floating legs must have an index specified"));
    }

    @Test
    @DisplayName("Fixed leg without a rate returns an error")
    public void testFixedLegWithoutARateReturnsAnError() {

        // Given
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setPayReceiveFlag("pay");
        leg1.setLegType("FIXED");
        leg1.setRate(null);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setPayReceiveFlag("receive");
        leg2.setLegType("FLOATING");
        leg2.setIndexName("LIBOR");

        List<TradeLegDTO> tradeLegDTOs = List.of(leg1,leg2);

        // When
        ValidationResult result = tradeLegValidator.validateTradeLegConsistency(tradeLegDTOs);

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsValue("Fixed legs must have rate greater than 0"));
    }

    @Test
    @DisplayName("Fixed leg a '-5.0' rate returns an error")
    public void testFixedLegWithNegativeRateReturnsAnError() {

        // Given
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setPayReceiveFlag("pay");
        leg1.setLegType("FIXED");
        leg1.setRate(-5.0);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setPayReceiveFlag("receive");
        leg2.setLegType("FLOATING");
        leg2.setIndexName("LIBOR");

        List<TradeLegDTO> tradeLegDTOs = List.of(leg1,leg2);

        // When
        ValidationResult result = tradeLegValidator.validateTradeLegConsistency(tradeLegDTOs);

        // Then
        assertFalse(result.isValid());
        assertEquals(1,result.getErrors().size());
        assertTrue(result.getErrors().containsValue("Fixed legs must have rate greater than 0"));
    }

    @Test
    @DisplayName("Multiple inconsistencies on legs returns all error messages")
    public void testMultipleErrors() {

        // Given
        TradeLegDTO leg1 = new TradeLegDTO();
        leg1.setPayReceiveFlag("pay");
        leg1.setLegType("FIXED");
        leg1.setRate(-5.0);

        TradeLegDTO leg2 = new TradeLegDTO();
        leg2.setPayReceiveFlag("pay");
        leg2.setLegType("FLOATING");
        leg2.setIndexName("");

        List<TradeLegDTO> tradeLegDTOs = List.of(leg1,leg2);

        // When
        ValidationResult result = tradeLegValidator.validateTradeLegConsistency(tradeLegDTOs);

        // Then
        assertFalse(result.isValid());
        assertEquals(3,result.getErrors().size());
        assertTrue(result.getErrors().containsValue("Legs must have opposite pay/receive flags"));
        assertTrue(result.getErrors().containsValue("Fixed legs must have rate greater than 0"));
        assertTrue(result.getErrors().containsValue("Floating legs must have an index specified"));
    }
}