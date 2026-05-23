package com.markwatson.info_spiders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for WebSpider.
 */
class WebClientTest {

  @Test
  @DisplayName("Spider markwatson.com and verify content is found")
  void testFetchAndDisplay() throws Exception {
    var client = new WebSpider("https://markwatson.com", 10);
    List<List<String>> results = client.getUrlContentLists();
    System.out.println("Found URIs: " + results);
    assertFalse(results.isEmpty(), "Should fetch at least one page from markwatson.com");
    for (List<String> entry : results) {
      assertEquals(2, entry.size(), "Each entry should contain [url, text]");
      assertFalse(entry.get(0).isBlank(), "URL should not be blank");
      assertFalse(entry.get(1).isBlank(), "Page text should not be blank");
    }
  }
}
