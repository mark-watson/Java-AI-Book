package com.markwatson.info_spiders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

/**
 * Copyright Mark Watson 2008-2020. All Rights Reserved.
 * License: Apache-2.0
 */

// Use Georgi Kobilarov's DBpedia lookup web service
//    ref: http://lookup.dbpedia.org/api/search.asmx?op=KeywordSearch
//    example: http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=Flagstaff&QueryClass=XML&MaxHits=10

/**
 * Searches return results that contain any of the search terms. I am going to filter
 * the results to ignore results that do not contain all search terms.
 */

public class DBpediaLookupClient {

  private final String query;
  private final List<Map<String, String>> variableBindings = new ArrayList<>();

  public DBpediaLookupClient(String query) throws Exception {
    this.query = query;
    System.out.println("\n query: " + query);

    String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
    System.out.println("\n encodedQuery: " + encodedQuery);

    String url = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=" + encodedQuery;

    try (var client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()) {

      var request = HttpRequest.newBuilder()
              .uri(URI.create(url))
              .timeout(Duration.ofSeconds(15))
              .header("User-Agent", "Mozilla/5.0 (compatible; JavaAIBook/1.0)")
              .GET()
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      Document doc = Jsoup.parse(response.body(), "", Parser.xmlParser());

      Elements results = doc.select("Result");
      for (Element result : results) {
        var binding = new HashMap<String, String>();

        Element label = result.selectFirst("Label");
        if (label != null && !label.text().isBlank()) {
          binding.put("Label", label.text());
        }

        Element uri = result.selectFirst("URI");
        if (uri != null && !uri.text().isBlank() && !uri.text().contains("Category")) {
          binding.put("URI", uri.text());
        }

        Element description = result.selectFirst("Description");
        if (description != null && !description.text().isBlank()) {
          binding.put("Description", description.text());
        }

        if (!variableBindings.contains(binding) && containsSearchTerms(binding)) {
          variableBindings.add(binding);
        }
      }
    }
  }

  public List<Map<String, String>> variableBindings() {
    return Collections.unmodifiableList(variableBindings);
  }

  private boolean containsSearchTerms(Map<String, String> bindings) {
    String text = String.join("", bindings.values()).toLowerCase();
    var tokenizer = new StringTokenizer(this.query);
    while (tokenizer.hasMoreTokens()) {
      if (!text.contains(tokenizer.nextToken().toLowerCase())) {
        return false;
      }
    }
    return true;
  }
}