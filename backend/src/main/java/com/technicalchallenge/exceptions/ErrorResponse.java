package com.technicalchallenge.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

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
