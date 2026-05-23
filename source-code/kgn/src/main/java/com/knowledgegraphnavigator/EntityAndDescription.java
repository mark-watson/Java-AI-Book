package com.knowledgegraphnavigator;

/**
 * Immutable data carrier for an entity name and its DBPedia URI.
 * Converted to a Java record for automatic toString(), equals(), hashCode().
 */
public record EntityAndDescription(String entityName, String entityUri) {
}
