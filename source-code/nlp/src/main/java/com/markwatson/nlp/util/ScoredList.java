package com.markwatson.nlp.util;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * <p/>
 * Copyright 2002-2008 by Mark Watson. All rights reserved.
 * <p/>
 * This software is not public domain. It can be legally
 * used under either of the following licenses:
 * <p/>
 * 1. KnowledgeBooks.com Non Commercial Royality Free License<br/>
 * 2. KnowledgeBooks.com Commercial Use License
 * <p/>
 * see www.knowledgebooks.com for details
 */
public class ScoredList {
    public List<String> strings = new ArrayList<>();
    private List<Integer> scores = new ArrayList<>();
    private int max_to_keep = 9999999;
    public ScoredList(int max_to_keep) {
        this.max_to_keep = max_to_keep;
    }

    /**
     * Create an empty scored list
     *
     */
    public ScoredList() { }

    /**
     * Add a value to a scored list
     *
     * @param text
     * @param score
     * @return true is a value was added. If the score is lower than
     * all currently stored values and the maximum number of values to store has
     * been reached, then the value will not be stored and the return value will be false.
     */
    public boolean addValue(String text, int score) {
        strings.add(text);
        scores.add(score);
        return removeOne(text);
    }

    /**
     * Add a value to a scored list. The "score" is not specified so
     * the "score" for a given value string is the number of times that it is added
     * to the scored list.
     *
     * @param text
     * @return true is a value was added. If the score is lower than
     * all currently stored values and the maximum number of values to store has
     * been reached, then the value will not be stored and the return value will be false.
     */
    public boolean addValue(String text) {
        int score = 1;
        if (strings.contains(text)) {
            int index = strings.indexOf(text);
            try { score = scores.get(index) + 1; } catch (Exception ignore) { ignore.printStackTrace(); }
            scores.set(index, score);
        } else {
            strings.add(text);
            scores.add(score);
        }
        return removeOne(text);
    }

    /**
     * Determine if a scored list contains a value string
     *
     * @param s
     * @return true if the value is stored in the scored list
     */
    public boolean contains(String s) { return strings.contains(s); }

    private boolean removeOne(String text) {    // WARNING: REALLY INEFFICIENT !!  TBD fix this
        int size = scores.size();
        if (size > max_to_keep) {
            int min_val = Integer.MAX_VALUE;
            int min_index = 0;
            for (int i = 0; i < size; i++) {
                int val = scores.get(i);
                if (val < min_val) {
                    min_val = val;
                    min_index = i;
                }
            }
            boolean ret = !text.equals(strings.get(min_index));
            strings.remove(min_index);
            scores.remove(min_index);
            return ret;
        } else {
            return true; // yes, the 'text' item was added
        }
    }

    /**
     * Sort a scored list in highest value first order.
     * Uses List.sort() for O(n log n) performance instead of bubble sort.
     */
    public void sort() {
        int size = strings.size();
        if (size <= 1) return;
        // Build index array and sort by score descending
        Integer[] indices = new Integer[size];
        for (int i = 0; i < size; i++) indices[i] = i;
        java.util.Arrays.sort(indices, (a, b) -> Integer.compare(scores.get(b), scores.get(a)));
        // Rebuild lists in sorted order
        var sortedStrings = new ArrayList<String>(size);
        var sortedScores = new ArrayList<Integer>(size);
        for (int idx : indices) {
            sortedStrings.add(strings.get(idx));
            sortedScores.add(scores.get(idx));
        }
        strings = sortedStrings;
        scores = sortedScores;
    }

    /**
     * Determining the number of values in a scored list
     *
     * @return the size of the scored list
     */
    public int size() { return strings.size(); }

    /**
     * Get a specific value
     *
     * @param index
     * @return the value at the specified index
     */
    public String getValue(int index) { return strings.get(index); }

    /**
     * Get a specific score
     *
     * @param index
     * @return the score at the specified index
     */
    public int getScore(int index) { return scores.get(index); }

    /**
     * Get all values and scores as a human readable string
     *
     * @return string
     */
    public String getValuesAsString() {
        return getValuesAsString(99999);
    }

    /**
     * Get a specified number of values as a human readable string
     *
     * @param max_return_values
     * @return string
     */
    public String getValuesAsString(int max_return_values) {
        var sb = new StringBuilder(200);
        sort();
        int limit = Math.min(strings.size(), max_return_values);
        for (int i = 0; i < limit; i++) {
            sb.append(strings.get(i)).append(":").append(scores.get(i));
            if (i != (limit - 1)) sb.append(", ");
        }
        return sb.toString();
    }
}
