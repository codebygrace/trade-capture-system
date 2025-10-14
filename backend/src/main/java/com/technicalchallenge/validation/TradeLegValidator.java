package com.technicalchallenge.validation;

import com.technicalchallenge.dto.TradeLegDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import static java.util.Objects.isNull;

@Service
public class TradeLegValidator {

    public ValidationResult validateTradeLegConsistency(List<TradeLegDTO> legs) {

        ValidationResult result = new ValidationResult();

        TradeLegDTO leg1 = legs.get(0);
        TradeLegDTO leg2 = legs.get(1);

        if (leg1.getPayReceiveFlag().equals(leg2.getPayReceiveFlag())) {
            result.addError("tradeLegs", "Legs must have opposite pay/receive flags");
        }

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
