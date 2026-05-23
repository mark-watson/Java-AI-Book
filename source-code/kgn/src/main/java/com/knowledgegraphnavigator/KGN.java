package com.knowledgegraphnavigator;

import com.markwatson.ner_dbpedia.TextToDbpediaUris;
import com.markwatson.semanticweb.QueryResult;

import static com.knowledgegraphnavigator.Log.out;
import static com.knowledgegraphnavigator.Log.sparql;
import static com.knowledgegraphnavigator.Log.clearSparql;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class KGN {

  private static final List<String> DEMOS_LIST = List.of(
      "Bill Gates and Melinda Gates worked at Microsoft",
      "IBM opened an office in Canada",
      "Steve Jobs worked at Apple Computer and visited IBM and Microsoft in Seattle");

  /** Single Scanner instance to avoid resource leaks from repeated System.in wrapping. */
  private final Scanner consoleScanner = new Scanner(System.in);

  public KGN() throws Exception {
    var endpoint = new Sparql();

    while (true) {
      String query = getUserQueryFromConsole();
      if (query == null || query.isBlank()) {
        out("Exiting KGN.");
        break;
      }
      out("\nProcessing query:\n" + query + "\n");
      if (query.equalsIgnoreCase("sparql")) {
        out("Generated SPARQL used to get current results:\n");
        out(sparql.toString());
        out("\n");
        clearSparql();
      } else {
        processQuery(endpoint, query);
      }
    }
  }

  private void processQuery(Sparql endpoint, String query) throws Exception {
    if (query.equalsIgnoreCase("demo")) {
      query = DEMOS_LIST.get(ThreadLocalRandom.current().nextInt(DEMOS_LIST.size()));
    }
    var kt = new TextToDbpediaUris(query);

    var userSelectedPeople = buildEntityList(kt.personNames, kt.personUris);
    var userSelectedCompanies = buildEntityList(kt.companyNames, kt.companyUris);

    if (!kt.cityNames.isEmpty()) {
      out("+++++ kt.cityNames:" + kt.cityNames.toString());
    }
    var userSelectedCities = buildEntityList(kt.cityNames, kt.cityUris);

    if (!kt.countryNames.isEmpty()) {
      out("+++++ kt.countryNames:" + kt.countryNames.toString());
    }
    var userSelectedCountries = buildEntityList(kt.countryNames, kt.countryUris);

    PrintEntityResearchResults.printResults(endpoint,
        userSelectedPeople,
        userSelectedCompanies,
        userSelectedCities,
        userSelectedCountries);

    for (var person1 : userSelectedPeople) {
      for (var person2 : userSelectedPeople) {
        if (person1 != person2) {
          QueryResult qr = EntityRelationships.results(endpoint, person1.entityUri(), person2.entityUri());
          if (!qr.rows.isEmpty()) {
            out("Relationships between person " + person1.entityName() +
                " person " + person2.entityName() + ":");
            out(qr.toString());
          }
        }
      }
    }
    //  Bill Gates, Melinda Gates and Steve Jobs at Apple Computer, IBM and Microsoft in Seattle
    for (var person : userSelectedPeople) {
      for (var company : userSelectedCompanies) {
        QueryResult qr = EntityRelationships.results(endpoint, person.entityUri(), company.entityUri());
        if (!qr.rows.isEmpty()) {
          out("Relationships between person " + person.entityName() +
              " company " + company.entityName() + ":");
          out(qr.toString());
        }
      }
    }
    for (var company1 : userSelectedCompanies) {
      for (var company2 : userSelectedCompanies) {
        if (company1 != company2) {
          QueryResult qr = EntityRelationships.results(endpoint, company1.entityUri(), company2.entityUri());
          if (!qr.rows.isEmpty()) {
            out("Relationships between company " + company1.entityName() +
                " company " + company2.entityName() + ":");
            out(qr.toString());
          }
        }
      }
    }
  }

  /**
   * Build a list of EntityAndDescription from parallel name/URI lists.
   */
  private static List<EntityAndDescription> buildEntityList(List<String> names, List<String> uris) {
    var result = new ArrayList<EntityAndDescription>();
    for (int i = 0; i < names.size(); i++) {
      result.add(new EntityAndDescription(names.get(i), uris.get(i)));
    }
    return result;
  }

  private String getUserQueryFromConsole() {
    out("Enter entities query:");
    if (consoleScanner.hasNextLine()) {
      return consoleScanner.nextLine();
    }
    return "";
  }

  public static void main(String[] args) throws Exception {
    new KGN();
  }
}
