package com.markwatson.info_spiders;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

/**
 * This simple web spider returns a list of lists, each containing two
 * strings representing "URL" and "text". Specifically, I do not return links on each page.
 */

/**
 * Copyright Mark Watson 2008-2020. All Rights Reserved.
 * License: Apache 2
 */

public class WebSpider {
  public WebSpider(String rootUrl, int maxReturnedPages) throws Exception {
    String host = URI.create(rootUrl).getHost();
    System.out.println("+ host: " + host);
    var urls = new ArrayList<String>();
    var alreadyVisited = new HashSet<String>();
    urls.add(rootUrl);
    int numFetched = 0;

    try (var httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()) {

      while (numFetched <= maxReturnedPages && !urls.isEmpty()) {
        try {
          System.out.println("+ urls: " + urls);
          String urlStr = urls.removeFirst();
          System.out.println("+ url_str: " + urlStr);
          if (urlStr.toLowerCase().contains(host) && !alreadyVisited.contains(urlStr)) {
            alreadyVisited.add(urlStr);

            var request = HttpRequest.newBuilder()
                    .uri(URI.create(urlStr))
                    .timeout(Duration.ofSeconds(15))
                    .header("User-Agent", "Mozilla/5.0 (compatible; JavaAIBook/1.0)")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
              System.out.println("Skipping " + urlStr + " (HTTP " + response.statusCode() + ")");
              continue;
            }

            Document doc = Jsoup.parse(response.body(), urlStr);
            numFetched++;
            String text = doc.text();

            // Skip any pages where text on page is identical to existing
            // page (e.g., http://example.com and http://example.com/index.html)
            boolean duplicate = urlContentLists.stream()
                    .anyMatch(ls -> text.equals(ls.get(1)));

            if (!duplicate) {
              try {
                Thread.sleep(500);
              } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
              }

              Elements anchors = doc.select("a[href]");
              for (Element anchor : anchors) {
                String linkStr = anchor.attr("abs:href");
                if (!linkStr.isEmpty()) {
                  urls.add(linkStr);
                }
              }
              urlContentLists.add(List.of(urlStr, text));
            }
          }
        } catch (IOException ex) {
          System.out.println("Error: " + ex);
          ex.printStackTrace();
        }
      }
    }
  }

  private final List<List<String>> urlContentLists = new ArrayList<>();

  public List<List<String>> getUrlContentLists() {
    return Collections.unmodifiableList(urlContentLists);
  }

  /** @deprecated Use {@link #getUrlContentLists()} instead. Kept for backward compatibility. */
  @Deprecated
  public List<List<String>> url_content_lists = urlContentLists;
}
