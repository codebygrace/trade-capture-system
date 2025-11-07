package com.technicalchallenge.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EntityType {

    TRADE("trade"),
    BOOK("book"),
    COUNTERPARTY("counterparty");

    private final String entityType;

    EntityType(String entityType) {
        this.entityType = entityType;
    }

    @JsonValue
    public String getEntityType() {
        return entityType;
    }

    @JsonCreator
    public static EntityType fromValue(String value) {
        for (EntityType type : EntityType.values()) {
            if (type.entityType.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid entity type value: " + value);
    }
}
