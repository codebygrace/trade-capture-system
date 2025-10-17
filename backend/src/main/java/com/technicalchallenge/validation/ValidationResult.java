package com.technicalchallenge.validation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;

@Getter
public class ValidationResult {

    private final Multimap<String, String> errors = LinkedListMultimap.create();

    public void addError(String field, String message) {
        errors.put(field, message);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}
