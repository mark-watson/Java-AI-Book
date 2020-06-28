# Information Gathering

TBD: introduction...

The following UML class diagram shows the public APIs the libraries developed in this chapter:

![UML class diagram for DBPedia lookup, Geonames, and web spiders](images/info-spider-uml.png)

## Web Scraping Examples

As a practical matter, much of the data that many people use for machine learning either comes from the web or from internal data sources. This short chapter provides some guidance and examples for getting text data from the web.

Before we start a technical discussion about web scraping I want to point out to you that much of the information on the web is copyright and the first thing that you should do is to read the terms of service for web sites to insure that your use of "scraped" or "spidered" data conforms with the wishes of the persons or organizations who own the content and pay to run scraped web sites.

### Motivation for Web Scraping

There is a huge amount of structured data available on the web via web services, semantic web/linked data markup, and APIs. That said, you will frequently find data that is useful to pull raw text from web sites but this text is usually fairly unstructured and in a messy (and frequently changing) format as web pages meant for human consumption and not meant to be ingested by software agents. In this chapter we will cover useful "web scraping" techniques. You will see that there is often a fair amount of work in dealing with different web design styles and layouts. To make things even more inconvenient you might find that your software information gathering agents will often break because of changes in web sites.

I tend to use one of three general techniques for scraping web sites. Only the first two will be covered in this chapter:

- Use an HTML parsing library that strips all HTML markup and Javascript from a page and returns a "pure text" block of text. The text in navigation menus, headers, etc. will be interspersed with what we might usually think of a "content" from a web site.
- Exploit HTML DOM (Document Object Model) formatting information on web sites to pick out headers, page titles, navigation menus, and large blocks of content text.
- Use a tool like (Selenium)[http://docs.seleniumhq.org/] to programaticly control a web browser so your software agents can login to site and otherwise perform navigation. In other words your software agents can simulate a human using a web browser.

I seldom need to use tools like Selenium but as the saying goes "when you need them, you need them." For simple sites I favor extracting all text as a single block and use DOM processing as needed.

I am not going to cover the use of Selenium and the Java Selenium Web-Driver APIs in this chapter because, as I mentioned, I tend to not use it frequently and I think that you are unlikely to need to do so either. I refer you to the Selenium documentation if the first two approaches in the last list do not work for your application. Selenium is primarily intended for building automating testing of complex web applications, so my occasional use in web spidering is not the common use case.

I assume that you have some experience with HTML and DOM. Since a DOM is a tree data structure it is useful to be able to collapse or to expand sub-trees in the DOM.

### Using the jsoup Library

We will use the MIT licensed library [jsoup](http://jsoup.org/). One reason I selected jsoup for the examples in this chapter out of many fine libraries that provide similar functionality is the particularly nice documentation, especially [The jsoup Cookbook](http://jsoup.org/cookbook/) which I urge you to bookmark as a general reference. In this chapter I will concentrate on just the most frequent web scraping use cases that I use in my own work.

The following bit of code uses jsoup to get the text inside all P (paragraph) elements that are direct children of any DIV element. On line 14 we use the jsoup library to fetch my home web page:


{lang="java",linenos=on}
~~~~~~~~
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
    Document doc = Jsoup.connect("http://democracynow.org").get();
    //Document doc = Jsoup.connect("http://abcnews.com/").get();
    Elements newsHeadlines = doc.select("div p");
    for (Element element : newsHeadlines) {
      System.out.println(" next element text: " + element.text());
    }
    String all_page_text = doc.text();
    System.out.println("All text on web page:\n" + all_page_text);
    Elements anchors = doc.select("a[href]");
    for (Element anchor : anchors) {
      String uri = anchor.attr("href");
      System.out.println(" next anchor uri: " + uri);
      System.out.println(" next anchor text: " + anchor.text());
    }
    Elements absolute_uri_anchors = doc.select("a[href]");
    for (Element anchor : absolute_uri_anchors) {
      String uri = anchor.attr("abs:href");
      System.out.println(" next anchor absolute uri: " + uri);
      System.out.println(" next anchor absolute text: " + anchor.text());
    }

  }
}
~~~~~~~~

In line ??? I am selecting the pattern that returns all P elements that are direct children of any DIV element and in lines ???? to ???? print the text inside these P elements.

For training data for machine learning it is useful to just grab all text on a web page and assume that common phrases dealing with web navigaion, etc. will be dropped from learned models because they occur in many different training examples for different classifications. In the above listing, lines ???-??? show how to fetch the plain text from an entire web page.

Output might look like:

{linenos=off}
~~~~~~~~
All text on web page:
Mark Watson: Consultant specializing in machine learning and artificial intelligence Toggle navigation Mark Watson Home page Consulting Free mentoring Blog Books Open Source Fun Consultant specializing in machine learning, artificial intelligence, cognitive computing, and web engineering...
~~~~~~~~

The 2gram (i.e., two words in sequence) "Toggle navigation" in the last listing has nothing to do with the real content in my site and is an artifact of using the Bootstrap CSS and Javascript tools. Often "noise" like this is simply ignored by machine learning models if it appears on many different sites but beware that this might be a problem and you might need to precisely fetch text from specific DOM elements. Similarly, notice that this last listing picks up the plain text from the navigation menus.

In the previous code listing, the code in lines ???-??? finds HTML anchor elements and prints the data associated with these elements.

Output will look something like this:

{linenos=on}
~~~~~~~~
 next anchor uri: #
 next anchor text: Mark Watson
 next anchor uri: /
 next anchor text: Home page
 next anchor uri: /consulting/
 next anchor text: Consulting
 next anchor uri: /mentoring/
 next anchor text: Free mentoring
 next anchor uri: http://blog.markwatson.com
 next anchor text: Blog
 next anchor uri: /books/
 next anchor text: Books
 next anchor uri: /opensource/
 next anchor text: Open Source
 next anchor uri: /fun/
 next anchor text: Fun
 next anchor uri: http://www.cognition.tech
 next anchor text: www.cognition.tech
 next anchor uri: https://github.com/mark-watson
 next anchor text: GitHub
 next anchor uri: https://plus.google.com/117612439870300277560
 next anchor text: Google+
 next anchor uri: https://twitter.com/mark_l_watson
 next anchor text: Twitter
 next anchor uri: http://www.freebase.com/m/0b6_g82
 next anchor text: Freebase
 next anchor uri: https://www.wikidata.org/wiki/Q18670263
 next anchor text: WikiData
 next anchor uri: https://leanpub.com/aijavascript
 next anchor text: Build Intelligent Systems with JavaScript
 next anchor uri: https://leanpub.com/lovinglisp
 next anchor text: Loving Common Lisp, or the Savvy Programmer's Secret Weapon
 next anchor uri: https://leanpub.com/javaai
 next anchor text: Practical Artificial Intelligence Programming With Java
 next anchor uri: http://markwatson.com/index.rdf
 next anchor text: XML RDF
 next anchor uri: http://markwatson.com/index.ttl
 next anchor text: Turtle RDF
 next anchor uri: https://www.wikidata.org/wiki/Q18670263
 next anchor text: WikiData
~~~~~~~~

Notice that there are different types of URIs like #, relative, and absolute. Any characters following a # character do not affect the routing of which web page is shown (or which API is called) but the characters after the # character are available for use in specifying anchor positions on a web page or extra parameters for API calls. Relative APIs like consulting/ (as seen in line 5) are understood to be relative to the base URI of the web site.


I often require that URIs be absolute URIs (i.e., starts with a protocol like "http:" or "https:") and lines 28-33 show how to select just absolute URI anchors. In line 30 I am specifying the attribute as "abs:href" to be more selective.
 
The output looks like:

{linenos=off}
~~~~~~~~
 next anchor absolute text: Mark Watson
 next anchor absolute uri: http://www.markwatson.com/
 next anchor absolute text: Home page
 next anchor absolute uri: http://www.markwatson.com/consulting/
 next anchor absolute text: Consulting
 next anchor absolute uri: http://www.markwatson.com/mentoring/
 next anchor absolute text: Free mentoring
 next anchor absolute uri: http://blog.markwatson.com
 next anchor absolute text: Blog
 ...
~~~~~~~~


## DBPedia Entity Lookup

TBD

The implementation file is DBpediaLookupClient.java:

{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.info_spiders;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
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


public class DBpediaLookupClient extends DefaultHandler {
  public DBpediaLookupClient(String query) throws Exception {
    this.query = query;
    //System.out.println("\n query: " + query);
    CloseableHttpClient client = HttpClients.createDefault();

    String query2 = query.replaceAll(" ", "+"); // URLEncoder.encode(query, "utf-8");
    //System.out.println("\n query2: " + query2);
/*
    HttpGet request = new HttpGet("http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=" +
        query2);

    // add request headers
    request.addHeader("custom-key", "mkyong");
    request.addHeader(HttpHeaders.USER_AGENT, "Googlebot");

    CloseableHttpResponse response = client.execute(request);
    HttpEntity entity = response.getEntity();
    if (entity != null) {
      // return it as a String
      String result = EntityUtils.toString(entity);
      System.out.println(result);
    }
*/
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser sax = factory.newSAXParser();
    sax.parse("http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=" +
        query2, this);

  }

  private List<Map<String, String>> variableBindings = new ArrayList<Map<String, String>>();
  private Map<String, String> tempBinding = null;
  private String lastElementName = null;

  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    //System.out.println("startElement " + qName);
    if (qName.equalsIgnoreCase("result")) {
      tempBinding = new HashMap<String, String>();
    }
    lastElementName = qName;
  }

  public void endElement(String uri, String localName, String qName) throws SAXException {
    //System.out.println("endElement " + qName);
    if (qName.equalsIgnoreCase("result")) {
      if (!variableBindings.contains(tempBinding) && containsSearchTerms(tempBinding))
        variableBindings.add(tempBinding);
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {
    String s = new String(ch, start, length).trim();
    //System.out.println("characters (lastElementName='" + lastElementName + "'): " + s);
    if (s.length() > 0) {
      if ("Description".equals(lastElementName)) {
        if (tempBinding.get("Description") == null) {
          tempBinding.put("Description", s);
        }
        tempBinding.put("Description", "" + tempBinding.get("Description") + " " + s);
      }
      //if ("URI".equals(lastElementName)) tempBinding.put("URI", s);
      if ("URI".equals(lastElementName) && s.indexOf("Category")==-1 && tempBinding.get("URI") == null) {
        tempBinding.put("URI", s);
      }
      if ("Label".equals(lastElementName)) tempBinding.put("Label", s);
    }
  }

  public List<Map<String, String>> variableBindings() {
    return variableBindings;
  }
  private boolean containsSearchTerms(Map<String, String> bindings) {
    StringBuilder sb = new StringBuilder();
    for (String value : bindings.values()) sb.append(value);  // do not need white space
    String text = sb.toString().toLowerCase();
    StringTokenizer st = new StringTokenizer(this.query);
    while (st.hasMoreTokens()) {
      if (text.indexOf(st.nextToken().toLowerCase()) == -1) {
        return false;
      }
    }
    return true;
  }
  private String query = "";
}
~~~~~~~~

TBD


## Client for GeoNames Service

TBD


The implementation file is GeoNamesClient.java:

{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.info_spiders;

import org.geonames.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright Mark Watson 2008-2020. All Rights Reserved.
 * License: Apache 2
 */

// You will need a free GeoNames account. Sign up:  https://www.geonames.org/login
// Then, set an environment variable: export GEONAMES=your-geonames-account-name

public class GeoNamesClient {
  public GeoNamesClient() {
  }

  private List<GeoNameData> helper(String name, String type) throws Exception {
    List<GeoNameData> ret = new ArrayList<GeoNameData>();

    String geonames_account_name = System.getenv("GEONAMES");
    if (geonames_account_name == null) {
      System.err.println("You will need a free GeoNames account.");
      System.err.println("Sign up:  https://www.geonames.org/login");
      System.err.println("Then, set an environment variable:");
      System.err.println("     export GEONAMES=your-geonames-account-name");
      throw new Exception("Need API key");
    }
    WebService.setUserName(System.getenv("GEONAMES"));

    ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
    searchCriteria.setStyle(Style.LONG);
    searchCriteria.setQ(name);
    ToponymSearchResult searchResult = WebService.search(searchCriteria);
    for (Toponym toponym : searchResult.getToponyms()) {
      //System.out.println("* " + toponym.getName() + " : " +toponym.getFeatureClassName());
      if (toponym.getFeatureClassName() != null &&
        toponym.getFeatureClassName().toString().indexOf(type) > -1 &&
        toponym.getName().indexOf(name) > -1 &&
        valid(toponym.getName())) {
        ret.add(new GeoNameData(toponym));
      }
    }
    return ret;
  }

  private boolean valid(String str) {
    if (str.contains("0")) return false;
    if (str.contains("1")) return false;
    if (str.contains("2")) return false;
    if (str.contains("3")) return false;
    if (str.contains("4")) return false;
    if (str.contains("5")) return false;
    if (str.contains("6")) return false;
    if (str.contains("7")) return false;
    if (str.contains("8")) return false;
    return !str.contains("9");
  }

  public List<GeoNameData> getCityData(String city_name) throws Exception {
    return helper(city_name, "city");
  }

  public List<GeoNameData> getCountryData(String country_name) throws Exception {
    return helper(country_name, "country");
  }

  public List<GeoNameData> getStateData(String state_name) throws Exception {
    List<GeoNameData> states = helper(state_name, "state");
    for (GeoNameData state : states) {
      state.geoType = GeoNameData.GeoType.STATE;
    }
    return states;
  }

  public List<GeoNameData> getRiverData(String river_name) throws Exception {
    return helper(river_name, "stream");
  }

  public List<GeoNameData> getMountainData(String mountain_name) throws Exception {
    return helper(mountain_name, "mountain");
  }
}
~~~~~~~~

TBD

The class **GeoNamesClient** in the last listing uses the class **GeoNameData**:

{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.info_spiders;

import org.geonames.Toponym;

/**
 * Copyright Mark Watson 2008-2020. All Rights Reserved.
 * License: Apache-2.0
 */
public class GeoNameData {
  public enum GeoType {
    CITY, COUNTRY, STATE, RIVER, MOUNTAIN, UNKNOWN
  }
  public int geoNameId = 0;
  public GeoType geoType = GeoType.UNKNOWN;
  public String name = "";
  public double latitude = 0;
  public double longitude = 0;
  public String countryCode = "";

  public GeoNameData(Toponym toponym) {
    geoNameId = toponym.getGeoNameId();
    latitude = toponym.getLatitude();
    longitude = toponym.getLongitude();
    name = toponym.getName();
    countryCode = toponym.getCountryCode();
    if (toponym.getFeatureClassName().startsWith("city")) geoType = GeoType.CITY;
    if (toponym.getFeatureClassName().startsWith("country")) geoType = GeoType.COUNTRY;
    if (toponym.getFeatureClassName().startsWith("state")) geoType = GeoType.STATE;
    if (toponym.getFeatureClassName().startsWith("stream")) geoType = GeoType.RIVER;
    if (toponym.getFeatureClassName().startsWith("mountain")) geoType = GeoType.MOUNTAIN;
  }

  public GeoNameData() {
  }

  public String toString() {
    return "[GeoNameData: " + name + ", type: " + geoType + ", country code: " + countryCode + ", ID: " + geoNameId + ", latitude: " + latitude + ", longitude: " + longitude + "]";
  }
}
~~~~~~~~

TBD

## Web Spidering

TBD

Here is another web spidering example that is different than the earlier example using the **jsoup** library. Here we will implement a spider using built in Java standard library network classes and also the Jericho HTML parser library.

{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.info_spiders;

import net.htmlparser.jericho.*;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
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
  public WebSpider(String root_url, int max_returned_pages) throws Exception {
    String host = new URL(root_url).getHost();
    System.out.println("+ host: " + host);
    List<String> urls = new ArrayList<String>();
    Set<String> already_visited = new HashSet<String>();
    urls.add(root_url);
    int num_fetched = 0;
    while (num_fetched <= max_returned_pages && !urls.isEmpty()) {
      try {
        System.out.println("+ urls: " + urls);
        String url_str = urls.remove(0);
        System.out.println("+ url_str: " + url_str);
        //if (url_str.toLowerCase().indexOf(host) > -1 && url_str.indexOf("https:") == -1 && !already_visited.contains(url_str)) {
        if (url_str.toLowerCase().indexOf(host) > -1 && !already_visited.contains(url_str)) {
          already_visited.add(url_str);
          URL url = new URL(url_str);
          URLConnection connection = url.openConnection();
          connection.setAllowUserInteraction(false);
          InputStream ins = url.openStream();
          Source source = new Source(ins);
          num_fetched++;
          TextExtractor te = new TextExtractor(source);
          String text = te.toString();
          // Skip any pages where text on page is identical to existing
          // page (e.g., http://example.com and http://exaple.com/index.html
          boolean process = true;
          for (List<String> ls : url_content_lists) {
            if (text.equals(ls.get(1))) {
              process = false;
              break;
            }
          }
          if (process) {
            try {
              Thread.sleep(500);
            } catch (Exception ignore) {
            }
            List<StartTag> anchorTags = source.getAllStartTags("a ");
            ListIterator iter = anchorTags.listIterator();
            while (iter.hasNext()) {
              StartTag anchor = (StartTag) iter.next();
              Attributes attr = anchor.parseAttributes();
              Attribute link = attr.get("href");
              String link_str = link.getValue();
              if (link_str.indexOf("http:") == -1) {
                String path = url.getPath();
                if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
                int index = path.lastIndexOf("/");
                if (index > -1) path = path.substring(0, index);
                link_str = url.getHost() + "/" + path + "/" + link_str;
                link_str = "http://" + link_str.replaceAll("///", "/").replaceAll("//", "/");
              }
              urls.add(link_str);
            }
            List<String> ls = new ArrayList<String>(2);
            ls.add(url_str);
            ls.add(text);
            url_content_lists.add(ls);
          }
        }
      } catch (Exception ex) {
        System.out.println("Error: " + ex);
        ex.printStackTrace();
      }
    }
  }

  public List<List<String>> url_content_lists = new ArrayList<List<String>>();
}
~~~~~~~~


TBD
