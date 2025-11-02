package com.technicalchallenge.model;

/**
 * Describes the actions that can be performed on a Trade or other entities
 */
public enum OperationType {

    /** Create a new entity */
    CREATE,

    /** Amend (update) an existing entity */
    AMEND,

    /** View (read) an existing entity */
    VIEW,

    /** Delete (cancel) an existing entity */
    DELETE,
}
