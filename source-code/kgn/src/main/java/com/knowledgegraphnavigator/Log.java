package com.knowledgegraphnavigator;

public class Log {
  public static void out(String s) { System.out.println(s); }
  /** Accumulated SPARQL queries for inspection. Note: not thread-safe (single-threaded demo). */
  public static final StringBuilder sparql = new StringBuilder();
  public static void clearSparql() { sparql.delete(0, sparql.length()); }
}
