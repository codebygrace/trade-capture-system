package com.technicalchallenge.service.validation;

import com.technicalchallenge.dto.TradeDTO;
import com.technicalchallenge.model.Book;
import com.technicalchallenge.model.Counterparty;
import com.technicalchallenge.repository.BookRepository;
import com.technicalchallenge.repository.CounterpartyRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Service that validates the business rules of a {@link TradeDTO} before it is processed further.
 * It incorporates the results of leg-specific consistency checks to {@link TradeLegValidator} and aggregates
 * any validation errors into a {@link ValidationResult} object
 */
@Service
public class TradeValidator {

    private final BookRepository bookRepository;
    private final CounterpartyRepository counterpartyRepository;
    private final TradeLegValidator tradeLegValidator;

    public TradeValidator(BookRepository bookRepository, CounterpartyRepository counterpartyRepository, TradeLegValidator tradeLegValidator) {
        this.bookRepository = bookRepository;
        this.counterpartyRepository = counterpartyRepository;
        this.tradeLegValidator = tradeLegValidator;
    }

    /**
     * Validates a {@link TradeDTO} against core business rules
     * @param tradeDTO the trade to be validated
     * @return a ValidationResult containing any validation errors that occur
     */
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
        } else {

            // Individual leg checks are delegated to TradeLegValidator
            ValidationResult legResult = tradeLegValidator.validateTradeLegConsistency(tradeDTO.getTradeLegs());
            result.addMultipleErrors(legResult.getErrors());
        }

        if (tradeDTO.getBookName() != null) {
            Optional<Book> book = (bookRepository.findByBookName(tradeDTO.getBookName()));
            Book foundBook = book.orElse(null);
            assert foundBook != null;
            if(!foundBook.isActive()) {
                result.addError("Book", "Counterparty must be active");
            }
        }

        if (tradeDTO.getCounterpartyName() != null) {
            Optional<Counterparty> counterparty = counterpartyRepository.findByName(tradeDTO.getCounterpartyName());
            Counterparty foundCounterparty = counterparty.orElse(null);
            assert foundCounterparty != null;
            if(!foundCounterparty.isActive())  {
                result.addError("Counterparty", "Counterparty must be active");
            }
        }

        return result;
    }
}
