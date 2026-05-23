package com.markwatson.ner_dbpedia;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class TextToDbpediaUris {

  /**
   * Represents a named-entity category with its lookup map and matched results.
   */
  private record EntityCategory(String name, Map<String, String> lookupMap,
                                Set<String> uriSet, List<String> uris, List<String> names) {
    void addIfAbsent(String uri, String ngram) {
      if (uriSet.add(uri)) {
        uris.add(uri);
        names.add(ngram);
      }
    }
  }

  // Precompiled patterns for tokenization
  private static final Pattern DOT   = Pattern.compile("\\.");
  private static final Pattern COMMA = Pattern.compile(",");
  private static final Pattern QMARK = Pattern.compile("\\?");
  private static final Pattern SEMI  = Pattern.compile(";");
  private static final Pattern NL    = Pattern.compile("\n");
  private static final Pattern MULTI_SPACE = Pattern.compile(" +");

  // Entity categories — order matters for priority
  private final List<EntityCategory> categories;

  // Public accessors preserving the original API
  public final List<String> personUris;
  public final List<String> personNames;
  public final List<String> companyUris;
  public final List<String> companyNames;
  public final List<String> cityUris;
  public final List<String> cityNames;
  public final List<String> countryUris;
  public final List<String> countryNames;
  public final List<String> broadcastNetworkUris;
  public final List<String> broadcastNetworkNames;
  public final List<String> musicGroupUris;
  public final List<String> musicGroupNames;
  public final List<String> politicalPartyUris;
  public final List<String> politicalPartyNames;
  public final List<String> tradeUnionUris;
  public final List<String> tradeUnionNames;
  public final List<String> universityUris;
  public final List<String> universityNames;

  @SuppressWarnings("unused")
  private TextToDbpediaUris() {
    this("");
  }

  public TextToDbpediaUris(String text) {
    // Initialize entity categories with their lookup maps
    var person           = makeCat("person",           NerMaps.personNames);
    var city             = makeCat("city",             NerMaps.cityNames);
    var company          = makeCat("company",          NerMaps.companyNames);
    var country          = makeCat("country",          NerMaps.countryNames);
    var broadcastNetwork = makeCat("broadcastNetwork", NerMaps.broadcastNetworks);
    var musicGroup       = makeCat("musicGroup",       NerMaps.musicGroupNames);
    var politicalParty   = makeCat("politicalParty",   NerMaps.politicalPartyNames);
    var tradeUnion       = makeCat("tradeUnion",       NerMaps.tradeUnionNames);
    var university       = makeCat("university",       NerMaps.universityNames);

    categories = List.of(person, city, company, country,
        broadcastNetwork, musicGroup, politicalParty, tradeUnion, university);

    // Wire public fields to category lists for backward compatibility
    personUris              = person.uris();
    personNames             = person.names();
    companyUris             = company.uris();
    companyNames            = company.names();
    cityUris                = city.uris();
    cityNames               = city.names();
    countryUris             = country.uris();
    countryNames            = country.names();
    broadcastNetworkUris    = broadcastNetwork.uris();
    broadcastNetworkNames   = broadcastNetwork.names();
    musicGroupUris          = musicGroup.uris();
    musicGroupNames         = musicGroup.names();
    politicalPartyUris      = politicalParty.uris();
    politicalPartyNames     = politicalParty.names();
    tradeUnionUris          = tradeUnion.uris();
    tradeUnionNames         = tradeUnion.names();
    universityUris          = university.uris();
    universityNames         = university.names();

    processText(text);
  }

  private static EntityCategory makeCat(String name, Map<String, String> lookupMap) {
    return new EntityCategory(name, lookupMap,
        new LinkedHashSet<>(), new ArrayList<>(), new ArrayList<>());
  }

  private void processText(String text) {
    String[] tokens = tokenize(text + " . . .");
    for (int i = 0, size = tokens.length - 2; i < size; i++) {
      String n3gram = tokens[i] + " " + tokens[i + 1] + " " + tokens[i + 2];
      String n2gram = tokens[i] + " " + tokens[i + 1];

      // Check 3-grams first (longest match wins)
      int skip = tryMatch(n3gram, 3, i);
      if (skip > 0) { i += skip - 1; continue; }

      // Check 2-grams
      skip = tryMatch(n2gram, 2, i);
      if (skip > 0) { i += skip - 1; continue; }

      // Check 1-grams
      tryMatch(tokens[i], 1, i);
    }
  }

  /**
   * Try to match an n-gram against all entity categories.
   * @return the number of extra tokens to skip (n-1), or 0 if no match.
   */
  private int tryMatch(String ngram, int n, int startIndex) {
    for (var cat : categories) {
      String uri = cat.lookupMap().get(ngram);
      if (uri != null) {
        if (!uri.startsWith("<")) uri = "<" + uri + ">";
        System.out.println(cat.name() + "\t" + startIndex + "\t" + (startIndex + n - 1) + "\t" + ngram + "\t" + uri);
        cat.addIfAbsent(uri, ngram);
        return n;
      }
    }
    return 0;
  }

  private String[] tokenize(String s) {
    String result = DOT.matcher(s).replaceAll(" . ");
    result = COMMA.matcher(result).replaceAll(" , ");
    result = QMARK.matcher(result).replaceAll(" ? ");
    result = NL.matcher(result).replaceAll(" ");
    result = SEMI.matcher(result).replaceAll(" ; ");
    return MULTI_SPACE.matcher(result).replaceAll(" ").split(" ");
  }

  @Override
  public String toString() {
    var sb = new StringBuilder("TextToDbpediaUris {\n");
    for (var cat : categories) {
      if (!cat.names().isEmpty()) {
        sb.append("  ").append(cat.name()).append(": ")
            .append(cat.names()).append(" -> ").append(cat.uris())
            .append('\n');
      }
    }
    sb.append('}');
    return sb.toString();
  }
}