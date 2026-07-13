package com.markwatson.semanticweb;

import java.sql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cache implements AutoCloseable {

  private static final Logger logger = LoggerFactory.getLogger(Cache.class);
  private static final String JDBC_URL = "jdbc:h2:./sparqlCache";

  private final Connection conn;

  public Cache() throws SQLException {
    conn = DriverManager.getConnection(JDBC_URL);
    conn.setAutoCommit(true);
    try (Statement s = conn.createStatement()) {
      s.execute("CREATE TABLE IF NOT EXISTS cache (query varchar(3000) PRIMARY KEY, result blob)");
    }
  }

  public void saveQueryResultInCache(String query, byte[] result) {
    try {
      if (fetchResultFromCache(query) != null) {
        return;
      }
      try (PreparedStatement ps =
          conn.prepareStatement("INSERT INTO cache (query, result) VALUES (?, ?)")) {
        ps.setString(1, query);
        ps.setBytes(2, result);
        ps.executeUpdate();
      }
    } catch (SQLException ex) {
      logger.error("Error saving query result to cache", ex);
    }
  }

  public byte[] fetchResultFromCache(String query) {
    try (PreparedStatement ps = conn.prepareStatement("SELECT result FROM cache WHERE query = ?")) {
      ps.setString(1, query);
      try (ResultSet rs = ps.executeQuery()) {
        if (!rs.next()) return null;
        return rs.getBytes(1);
      }
    } catch (SQLException ex) {
      logger.error("Error fetching result from cache", ex);
      return null;
    }
  }

  @Override
  public void close() throws SQLException {
    if (conn != null && !conn.isClosed()) {
      conn.close();
    }
  }
}
