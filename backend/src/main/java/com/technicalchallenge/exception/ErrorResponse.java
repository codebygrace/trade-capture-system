package com.technicalchallenge.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Standard structure for error responses returned by the API.
 * Helps to ensure a consistent format which includes status code, error, message and timestamp
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private int statusCode;
    private String error;
    private String message;
    OffsetDateTime timestamp;

    public ErrorResponse(String message) {
        this.message = message;
    }
}
