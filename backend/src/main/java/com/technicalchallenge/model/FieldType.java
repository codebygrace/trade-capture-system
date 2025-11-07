package com.technicalchallenge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FieldType {

    STRING("string"),
    NUMBER("number"),
    DATE("date"),
    BOOLEAN("boolean");

    private final String fieldType;

    FieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    @JsonValue
    public String getFieldType() {
        return fieldType;
    }

    @JsonCreator
    public static FieldType fromValue(String value) {
        for (FieldType fieldType : FieldType.values()) {
            if (fieldType.getFieldType().equals(value)) {
                return fieldType;
            }
        }
        throw new IllegalArgumentException("Invalid field type value: " + value);
    }
}
