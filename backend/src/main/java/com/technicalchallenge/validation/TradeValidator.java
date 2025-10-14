package com.technicalchallenge.validation;

import com.technicalchallenge.dto.TradeDTO;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class TradeValidator {

    public ValidationResult validateTradeBusinessRules(TradeDTO tradeDTO) {

        ValidationResult result = new ValidationResult();

        LocalDate maturityDate = tradeDTO.getTradeMaturityDate();
        LocalDate tradeDate = tradeDTO.getTradeDate();
        LocalDate startDate = tradeDTO.getTradeStartDate();

        if ( maturityDate != null && startDate != null && tradeDate != null) {

            if (maturityDate.isBefore(startDate)) {
                result.addError("tradeMaturityDate", "Maturity date cannot be before start date");
            }

            if (maturityDate.isBefore(tradeDate)) {
                result.addError("tradeMaturityDate", "Maturity date cannot be before trade date");
            }

            if (startDate.isBefore(tradeDate)) {
                result.addError("tradeStartDate", "Start date cannot be before trade date");
            }

            if (tradeDate.isBefore(LocalDate.now().minusDays(30))) {
                result.addError("tradeDate", "Trade date cannot be more than 30 days in the past");
            }
        }

            if (tradeDTO.getTradeLegs() == null || tradeDTO.getTradeLegs().size() != 2) {
                result.addError("tradeLegs", "Trade legs must have exactly 2 legs");
            }

        return result;
    }
}
