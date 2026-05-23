package com.markwatson.nlp.util;

/**
 * A simple name-value pair. Implemented as a Java record for immutability
 * and conciseness.
 *
 * @param name  the name/key
 * @param value the value
 * @param <K>   key type
 * @param <V>   value type
 */
public record NameValue<K, V>(K name, V value) {
    // Accessor aliases to match the old API: getName() -> name(), getValue() -> value()
    public K getName() { return name; }
    public V getValue() { return value; }

    @Override
    public String toString() {
    	return "[NameValue: " + name + " : " + value + "]";
    }
}