package com.technicalchallenge.service.validation;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;

/**
 * Represents the result of a validation process.
 * Uses a multimap so that each field can be associated with multiple error messages.
 */
@Getter
public class ValidationResult {

    /** LinkedListMultimap allows a field to have multiple validation error messages */
    private final Multimap<String, String> errors = LinkedListMultimap.create();

    /**
     * Records a single validation error for a specific field. Multiple error messages can be associated with same field.
     * Each call to this method appends the new error message without overwriting existing ones.
     * @param field name of the field
     * @param message error message describing the validation problem
     */
    public void addError(String field, String message) {
        errors.put(field, message);
    }

    /**
     * Adds all field and validation error messages from the given multimap to the existing error collection.
     * Existing keys are preserved and their associated values are appended. It allows for multiple values per key.
     * @param errorMultiMap a multimap where key represents the field name and each value is an individual validation error message.
     *                      If the map is empty then there is no effect.
     */
    public void addMultipleErrors(Multimap<String, String> errorMultiMap) {
        errors.putAll(errorMultiMap);
    }

    /**
     * Checks if there are any errors
     * @return true mean there are no error and false if otherwise
     */
    public boolean isValid() {
        return errors.isEmpty();
    }
}
