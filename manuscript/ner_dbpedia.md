# Resolve Entity Names to DBPedia References

As a personal research project, I have collected a large set of mapping of entity names (e.g., people's names, city names, names of music groups, company names, etc.) to the DBPedia URI for that entity. I have developed libraries to use this data in Common Lisp, Haskell, and Java. Here we use the Java version of this library.

The Java library is found in the directory **ner_dbpedia*. The raw data for these entity to URI mappings are found in the directory **ner_dbpedia/dbpedia_as_text**.

TBD

![Overview of Java Class UML Diagram for this Example](images/nerdbpedia-uml.png)


## DBPedia Entities

TBD

## Library Implementation

TBD


![IDE View of Project](images/nerdbpedia-ide.png)

The class **com.markwatson.ner_dbpedia.NerMaps** is a utility for reading the raw entity mapping data files and creating hash tables for these mappings:

{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.ner_dbpedia;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Copyright Mark Watson 2020. Apache 2 license,
 */
public class NerMaps {

  private static String theModelPath = null;
  static {
    theModelPath = getModelPath();
  }

  private static String enforceAngleBrackets(String s) {
    if (s.startsWith("<")) return s;
    return "<" + s + ">";
  }
  private static Map<String, String> textFileToMap(String nerFileName) {
    Map<String, String> ret = new HashMap<String, String>();
    try {
      Stream<String> lines =
          Files.lines(Paths.get(theModelPath, nerFileName));
      lines.forEach(line -> {
        String[] tokens = line.split("\t");
        if (tokens.length > 1) {
          ret.put(tokens[0], enforceAngleBrackets(tokens[1]));
        }
      });
      lines.close();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return ret;
  }

  static public final Map<String, String> broadcastNetworks = textFileToMap("BroadcastNetworkNamesDbPedia.txt");
  static public final Map<String, String> cityNames = textFileToMap("CityNamesDbpedia.txt");
  static public final Map<String, String> companyames = textFileToMap("CompanyNamesDbPedia.txt");
  static public final Map<String, String> countryNames = textFileToMap("CountryNamesDbpedia.txt");
  static public final Map<String, String> musicGroupNames = textFileToMap("MusicGroupNamesDbPedia.txt");
  static public final Map<String, String> personNames = textFileToMap("PeopleDbPedia.txt");
  static public final Map<String, String> politicalPartyNames = textFileToMap("PoliticalPartyNamesDbPedia.txt");
  static public final Map<String, String> tradeUnionNames = textFileToMap("TradeUnionNamesDbPedia.txt");
  static public final Map<String, String> universityNames = textFileToMap("UniversityNamesDbPedia.txt");

  private static String getModelPath() {
    String ret = "dbpedia_as_text/";

    try {
      new FileInputStream("dbpedia_as_text/PeopleDbPedia.txt");
    } catch (FileNotFoundException var4) {
      try {
        new FileInputStream("../ner_dbpedia/dbpedia_as_text/PeopleDbPedia.txt");
        ret = "../ner_dbpedia/dbpedia_as_text/";
      } catch (FileNotFoundException var3) {
        System.err.println("Error: can not find named entity files model files, looking in dbpedia_as_text/ and ../ner_dbpedia/dbpedia_as_text/");
      }
    }
    return ret;
  }

  public static void main(String[] args) throws IOException {
    new NerMaps().textFileToMap("CityNamesDbpedia.txt");
  }
}
~~~~~~~~


The class com.markwatson.ner_dbpedia.TextToDbpediaUris processes an input string. We will use this code in the chapter *Automatically Generating Data for Knowledge Graphs*.

The code in the class **TextToDbpediaUris** is simple and repeats two common patterns for each entity type. We will only look at some of the code here.

{lang="java",linenos=off}
~~~~~~~~
package com.markwatson.ner_dbpedia;

import java.util.ArrayList;
import java.util.List;

public class TextToDbpediaUris {
  private TextToDbpediaUris() {
  }

  public List<String> personUris = new ArrayList<String>();
  public List<String> personNames = new ArrayList<String>();
  public List<String> companyUris = new ArrayList<String>();
  public List<String> companyNames = new ArrayList<>();
~~~~~~~~



{lang="java",linenos=on}
~~~~~~~~
  public TextToDbpediaUris(String text) {
    String[] tokens = tokenize(text + " . . .");
    String s = "";
    for (int i = 0, size = tokens.length - 2; i < size; i++) {
      String n2gram = tokens[i] + " " + tokens[i + 1];
      String n3gram = n2gram + " " + tokens[i + 2];
      // check for 3grams:
      if ((s = NerMaps.personNames.get(n3gram)) != null) {
        log("person", i, i + 2, n3gram, s);
        i += 2;
        continue;
      }
~~~~~~~~


{lang="java",linenos=off}
~~~~~~~~
      // check for 2grams:
      if ((s = NerMaps.personNames.get(n2gram)) != null) {
        log("person", i, i + 1, n2gram, s);
        i += 1;
        continue;
      }
~~~~~~~~


{lang="java",linenos=off}
~~~~~~~~
  public void log(String nerType, int index1, int index2, String ngram, String uri) {
    System.out.println(nerType + "\t" + index1 + "\t" + index2 + "\t" + ngram + "\t" + uri);
    if (!uri.startsWith("<")) uri = "<" + uri + ">";
    if (nerType.equals("person")) {
      if (!personUris.contains(uri)) {
        personUris.add(uri);
        personNames.add(ngram);
      }
    }
~~~~~~~~

{lang="java",linenos=off}
~~~~~~~~
  private String[] tokenize(String s) {
    return s.replaceAll("\\.", " \\. ").
             replaceAll(",", " , ").
             replaceAll("\\?", " ? ").
             replaceAll("\n", " ").
             replaceAll(";", " ; ").split(" ");
  }
~~~~~~~~

Here is the unit test code:

{lang="java",linenos=off}
~~~~~~~~
package com.markwatson.ner_dbpedia;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TextToDbpediaUrisTest extends TestCase {
  /**
   * Create the test case
   *
   * @param testName name of the test case
   */
  public TextToDbpediaUrisTest(String testName)
  {
    super( testName );
  }

  /**
   * @return the suite of tests being tested
   */
  public static Test suite()
  {
    return new TestSuite( TextToDbpediaUrisTest.class );
  }

  /**
   * Test that is just for side effect printouts:
   */
  public void test1() throws Exception {
    String s = "PTL Satellite Network covered President Bill Clinton going to Guatemala and visiting the Coca Cola Company.";
    TextToDbpediaUris test = new TextToDbpediaUris(s);
    System.out.println(test);
    assert(true);
  }
}
~~~~~~~~

Here is the output from running the unit test code:

{linenos=off}
~~~~~~~~
broadcastNetwork	0	2	PTL Satellite Network	<http://dbpedia.org/resource/PTL_Satellite_Network>
person	5	6	Bill Clinton	<http://dbpedia.org/resource/Bill_Clinton>
country	9	10	Guatemala	<http://dbpedia.org/resource/Guatemala>
company	13	14	Coca Cola	<http://dbpedia.org/resource/Coca-Cola>
~~~~~~~~

{lang="java",linenos=off}
~~~~~~~~
~~~~~~~~
