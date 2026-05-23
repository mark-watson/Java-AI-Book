package com.knowledgegraphnavigator;

import com.markwatson.semanticweb.QueryResult;

import java.sql.SQLException; // Cache layer in JenaApis library throws this

public class EntityDetail {

  public static QueryResult genericResults(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    var query = """
        SELECT DISTINCT ?p ?o WHERE {
          %s ?p ?o .
          FILTER (!regex(str(?p), 'wiki', 'i'))
        } LIMIT 10
        """.formatted(entityUri);
    return endpoint.query(query);
  }

  public static String genericAsString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = genericResults(endpoint, entityUri);
    return qr.toString();
  }

  public static QueryResult cityResults(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    var query = cityTemplate.formatted(entityUri, entityUri, entityUri, entityUri, entityUri);
    return endpoint.query(query);
  }

  public static String cityAsString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = cityResults(endpoint, entityUri);
    return qr.toString();
  }

  public static QueryResult countryResults(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    var query = countryTemplate.formatted(entityUri, entityUri, entityUri, entityUri, entityUri);
    return endpoint.query(query);
  }

  public static String countryAsString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = countryResults(endpoint, entityUri);
    return qr.toString();
  }

  public static QueryResult personResults(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    var query = personTemplate.formatted(entityUri, entityUri, entityUri, entityUri, entityUri);
    return endpoint.query(query);
  }

  public static String personAsString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = personResults(endpoint, entityUri);
    return qr.toString();
  }

  public static QueryResult companyResults(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    var query = companyTemplate.formatted(entityUri, entityUri, entityUri, entityUri, entityUri);
    return endpoint.query(query);
  }

  public static String companyAsString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = companyResults(endpoint, entityUri);
    return qr.toString();
  }

  private static final String companyTemplate = """
      SELECT DISTINCT
          (GROUP_CONCAT (DISTINCT ?industry2; SEPARATOR=' | ') AS ?industry)
          (GROUP_CONCAT (DISTINCT ?netIncome2; SEPARATOR=' | ') AS ?netIncome)
          (GROUP_CONCAT (DISTINCT ?label2; SEPARATOR=' | ') AS ?label)
          (GROUP_CONCAT (DISTINCT ?comment2; SEPARATOR=' | ') AS ?comment)
          (GROUP_CONCAT (DISTINCT ?numberOfEmployees2; SEPARATOR=' | ') AS ?numberOfEmployees) {
        %s <http://www.w3.org/2000/01/rdf-schema#comment>  ?comment2 .
                  FILTER  (lang(?comment2) = 'en') .
        OPTIONAL { %s <http://dbpedia.org/ontology/industry> ?industry2 } .
        OPTIONAL { %s <http://dbpedia.org/ontology/netIncome> ?netIncome2 } .
        OPTIONAL { %s <http://dbpedia.org/ontology/numberOfEmployees> ?numberOfEmployees2 } .
        OPTIONAL { %s <http://www.w3.org/2000/01/rdf-schema#label> ?label2 .
                  FILTER (lang(?label2) = 'en') }
      } LIMIT 30""";

  private static final String personTemplate = """
      SELECT DISTINCT
          (GROUP_CONCAT (DISTINCT ?birthplace2; SEPARATOR=' | ') AS ?birthplace)
          (GROUP_CONCAT (DISTINCT ?label2; SEPARATOR=' | ') AS ?label)
          (GROUP_CONCAT (DISTINCT ?comment2; SEPARATOR=' | ') AS ?comment)
          (GROUP_CONCAT (DISTINCT ?almamater2; SEPARATOR=' | ') AS ?almamater)
          (GROUP_CONCAT (DISTINCT ?spouse2; SEPARATOR=' | ') AS ?spouse) {
        %s <http://www.w3.org/2000/01/rdf-schema#comment>  ?comment2 .
        FILTER  (lang(?comment2) = 'en') .
        OPTIONAL { %s <http://dbpedia.org/ontology/birthPlace> ?birthplace2 } .
        OPTIONAL { %s <http://dbpedia.org/ontology/almaMater> ?almamater2 } .
        OPTIONAL { %s <http://dbpedia.org/ontology/spouse> ?spouse2 } .
        OPTIONAL { %s  <http://www.w3.org/2000/01/rdf-schema#label> ?label2 .
          FILTER  (lang(?label2) = 'en') }
      } LIMIT 10""";

  private static final String countryTemplate = """
      SELECT DISTINCT
          (GROUP_CONCAT (DISTINCT ?areaTotal2; SEPARATOR=' | ') AS ?areaTotal)
          (GROUP_CONCAT (DISTINCT ?label2; SEPARATOR=' | ') AS ?label)
          (GROUP_CONCAT (DISTINCT ?comment2; SEPARATOR=' | ') AS ?comment)
          (GROUP_CONCAT (DISTINCT ?populationDensity2; SEPARATOR=' | ') AS ?populationDensity) {
        %s <http://www.w3.org/2000/01/rdf-schema#comment>  ?comment2 .
                             FILTER  (lang(?comment2) = 'en') .
                       OPTIONAL { %s <http://dbpedia.org/ontology/areaTotal> ?areaTotal2 } .
                       OPTIONAL { %s <http://dbpedia.org/ontology/populationDensity> ?populationDensity2 } .
                       OPTIONAL { %s <http://www.w3.org/2000/01/rdf-schema#label> ?label2 . }
                     } LIMIT 30""";

  private static final String cityTemplate = """
      SELECT DISTINCT
          (GROUP_CONCAT (DISTINCT ?latitude_longitude2; SEPARATOR=' | ')
              AS ?latitude_longitude)
          (GROUP_CONCAT (DISTINCT ?populationDensity2; SEPARATOR=' | ') AS ?populationDensity)
          (GROUP_CONCAT (DISTINCT ?label2; SEPARATOR=' | ') AS ?label)
          (GROUP_CONCAT (DISTINCT ?comment2; SEPARATOR=' | ') AS ?comment)
          (GROUP_CONCAT (DISTINCT ?country2; SEPARATOR=' | ') AS ?country) {
        %s <http://www.w3.org/2000/01/rdf-schema#comment>  ?comment2 . FILTER  (lang(?comment2) = 'en') .
        OPTIONAL { %s <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?latitude_longitude2 } .
        OPTIONAL { %s <http://dbpedia.org/ontology/PopulatedPlace/populationDensity> ?populationDensity2 } .
        OPTIONAL { %s <http://dbpedia.org/ontology/country> ?country2 } .
        OPTIONAL { %s <http://www.w3.org/2000/01/rdf-schema#label> ?label2 .
                   FILTER  (lang(?label2) = 'en') }
      } LIMIT 30""";

}
