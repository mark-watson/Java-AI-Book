package com.markwatson.semanticweb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class QueryResult implements Serializable {
  private QueryResult() { }
  public QueryResult(List<String> variableList) {
    this.variableList = List.copyOf(variableList);
  }
  public List<String> variableList;
  public List<List<String>> rows = new ArrayList<>();

  public List<String> getVariableList() {
    return variableList;
  }

  public List<List<String>> getRows() {
    return rows;
  }

  public String toString() {
    var sb = new StringBuilder("[QueryResult vars:" + variableList + "\nRows:\n");
    for (List<String> row : rows) {
      sb.append("  ").append(row).append("\n");
    }
    return sb.toString();
  }
}
