package com.technicalchallenge.service.validation;

import com.technicalchallenge.dto.TradeLegDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import static java.util.Objects.isNull;

/**
 * Service that validates the consistency of a pair of {@link TradeLegDTO} objects
 * Checks need to be satisfied before a trade can proceed with being processes
 */
@Service
public class TradeLegValidator {

    /**
     * Validates that a pair of {@link TradeLegDTO} objects satisfy consistency rules
     * @param legs list of 2 TradeLegDTOs
     * @return a ValidationResult containing any errors discovered
     */
    public ValidationResult validateTradeLegConsistency(List<TradeLegDTO> legs) {

        ValidationResult result = new ValidationResult();

        TradeLegDTO leg1 = legs.get(0);
        TradeLegDTO leg2 = legs.get(1);

        // Checks if legs have opposite pay/receive flags
        if (leg1.getPayReceiveFlag().equals(leg2.getPayReceiveFlag())) {
            result.addError("tradeLegs", "Legs must have opposite pay/receive flags");
        }

        // Loops through both legs to check index is specified for floating and fixed legs have a valid rate
        for (TradeLegDTO leg : legs) {
            if (leg.getLegType().equals("Floating") && (leg.getIndexName().isBlank()) || isNull(leg.getIndexName())) {
                result.addError("tradeLegs", "Leg must have an index specified");
            }
            if (leg.getLegType().equals("Fixed") && (leg.getRate() <= 0)) {
                result.addError("tradeLegs", "Leg must have rate greater than 0");
            }
        }

        return result;
    }
}
