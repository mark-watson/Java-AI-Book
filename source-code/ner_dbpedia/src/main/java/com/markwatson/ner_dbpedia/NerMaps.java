package com.markwatson.ner_dbpedia;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright Mark Watson 2020. Apache 2 license,
 */
public class NerMaps {

  private static String enforceAngleBrackets(String s) {
    if (s.startsWith("<")) return s;
    return "<" + s + ">";
  }

  private static Map<String, String> textFileToMap(String nerFileName) {
    var ret = new HashMap<String, String>();
    try (InputStream in = ClassLoader.getSystemResourceAsStream(nerFileName);
         BufferedReader reader = new BufferedReader(
             new InputStreamReader(in, StandardCharsets.UTF_8))) {
      reader.lines().forEach(line -> {
        String[] tokens = line.split("\t");
        if (tokens.length > 1) {
          ret.put(tokens[0], enforceAngleBrackets(tokens[1]));
        }
      });
    } catch (IOException ex) {
      throw new UncheckedIOException(
          "Failed to load NER resource file: " + nerFileName, ex);
    }
    return Map.copyOf(ret);
  }

  static public final Map<String, String> broadcastNetworks = textFileToMap("BroadcastNetworkNamesDbPedia.txt");
  static public final Map<String, String> cityNames = textFileToMap("CityNamesDbpedia.txt");
  static public final Map<String, String> companyNames = textFileToMap("CompanyNamesDbPedia.txt");
  static public final Map<String, String> countryNames = textFileToMap("CountryNamesDbpedia.txt");
  static public final Map<String, String> musicGroupNames = textFileToMap("MusicGroupNamesDbPedia.txt");
  static public final Map<String, String> personNames = textFileToMap("PeopleDbPedia.txt");
  static public final Map<String, String> politicalPartyNames = textFileToMap("PoliticalPartyNamesDbPedia.txt");
  static public final Map<String, String> tradeUnionNames = textFileToMap("TradeUnionNamesDbPedia.txt");
  static public final Map<String, String> universityNames = textFileToMap("UniversityNamesDbPedia.txt");

  /**
   * Keep legacy field name as an alias so downstream code that references
   * {@code NerMaps.companyames} (the original typo) still compiles.
   */
  @Deprecated(forRemoval = true)
  static public final Map<String, String> companyames = companyNames;

  public static void main(String[] args) {
    System.out.println(
        textFileToMap("CityNamesDbpedia.txt"));
  }
}
