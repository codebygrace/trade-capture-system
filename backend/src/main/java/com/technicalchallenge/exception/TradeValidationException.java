package com.technicalchallenge.exception;

import com.google.common.collect.Multimap;
import lombok.Getter;

/**
 * Custom exception thrown when a trade fails validation checks. It carries a Multimap of validation errors which helps
 * users to see which fields have failed and why
 */
@Getter
public class TradeValidationException extends RuntimeException {

    /** A multimap containing validation errors  */
    private final Multimap<String,String> errors;

    /**
     * Creates a new TradeValidationException
     * @param message short description of the overall validation failure
     * @param errors a Multimap of fields with their associated validation errors
     */
    public TradeValidationException(String message, Multimap<String,String> errors) {
        super(message);
        this.errors = errors;
    }
}
