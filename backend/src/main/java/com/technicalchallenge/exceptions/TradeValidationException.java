package com.technicalchallenge.exceptions;

import com.google.common.collect.Multimap;
import lombok.Getter;

@Getter
public class TradeValidationException extends RuntimeException {

    private final Multimap<String,String> errors;

    public TradeValidationException(String message, Multimap<String,String> errors) {
        super(message);
        this.errors = errors;
    }
}
