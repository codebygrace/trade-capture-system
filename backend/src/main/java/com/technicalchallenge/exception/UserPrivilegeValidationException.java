package com.technicalchallenge.exception;

/**
 * Custom exception thrown when a user fails validation checks.
 */
public class UserPrivilegeValidationException extends RuntimeException {
    public UserPrivilegeValidationException(String message) {
        super(message);
    }
}
