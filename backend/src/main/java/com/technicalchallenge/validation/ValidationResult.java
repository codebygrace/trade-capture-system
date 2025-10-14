package com.technicalchallenge.validation;

import lombok.Getter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ValidationResult {

    private final Map<String, List<String>> errors = new HashMap<>();

    public void addError(String field, String message) {
        errors.put(field, List.of(message));
    }

    public boolean isValid() {
        return errors.isEmpty();
    }
}
