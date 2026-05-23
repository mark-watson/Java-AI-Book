package com.markwatson.web_scraping;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Examples of using jsoup
 */
public class MySitesExamples {

  public static void main(String[] args) throws Exception {
    Document doc = Jsoup.connect("https://markwatson.com")
        .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 14_0; rv:120.0) Gecko/20100101 Firefox/120.0")
        .timeout(5000).get();
    Elements newsHeadlines = doc.select("div p");
    for (var element : newsHeadlines) {
      System.out.println(" next element text: " + element.text());
    }
    String allPageText = doc.text();
    System.out.println("All text on web page:\n" + allPageText);
    Elements anchors = doc.select("a[href]");
    for (var anchor : anchors) {
      String uri = anchor.attr("href");
      System.out.println(" next anchor uri: " + uri);
      System.out.println(" next anchor text: " + anchor.text());
    }
    Elements absoluteUriAnchors = doc.select("a[href]");
    for (var anchor : absoluteUriAnchors) {
      String uri = anchor.attr("abs:href");
      System.out.println(" next anchor absolute uri: " + uri);
      System.out.println(" next anchor absolute text: " + anchor.text());
    }

  }
}
