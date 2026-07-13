package com.markwatson.semanticweb;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JenaApisTest {

  @Test
  @DisplayName("Remote SPARQL query against DBPedia with caching")
  void testRemoteSparqlQuery() throws Exception {
    try (var jenaApis = new JenaApis()) {
      // test remote SPARQL queries against DBPedia SPARQL endpoint
      QueryResult qrRemote =
          jenaApis.queryRemote(
              "https://dbpedia.org/sparql",
              """
              SELECT ?p WHERE {
                <http://dbpedia.org/resource/Bill_Gates> ?p <http://dbpedia.org/resource/Microsoft> .
              } LIMIT 10\
              """);
      System.out.println("qrRemote:" + qrRemote);
      assertNotNull(qrRemote, "Remote query result should not be null");
      assertFalse(qrRemote.getVariableList().isEmpty(), "Should have at least one variable");

      System.out.println("Repeat query to test caching:");
      qrRemote =
          jenaApis.queryRemote(
              "https://dbpedia.org/sparql",
              "select distinct ?s { ?s ?p <http://dbpedia.org/resource/Parks> } LIMIT 10");
      System.out.println("qrRemote (hopefully from cache):" + qrRemote);
      assertNotNull(qrRemote, "Cached query result should not be null");

      jenaApis.loadRdfFile("data/rdfs_business.nt");
      jenaApis.loadRdfFile("data/sample_news.nt");
      jenaApis.loadRdfFile("data/sample_news.n3");

      QueryResult qr =
          jenaApis.query("select ?s ?o where { ?s <http://knowledgebooks.com/title> ?o } limit 15");
      System.out.println("qr:" + qr);
      assertNotNull(qr, "Local query result should not be null");

      jenaApis.saveModelToTurtleFormat("model_save.nt");
      jenaApis.saveModelToN3Format("model_save.n3");
    }
  }

  @Test
  @DisplayName("OWL reasoning with RDFS inference")
  void testOwlReasoning() throws Exception {
    try (var jenaApis = new JenaApis()) {
      jenaApis.loadRdfFile("data/news.n3");

      QueryResult qr =
          jenaApis.query(
              """
              PREFIX kb: <http://knowledgebooks.com/ontology#>
              SELECT ?s ?o WHERE { ?s kb:containsCity ?o }\
              """);
      System.out.println("qr:" + qr);
      assertNotNull(qr, "OWL query result should not be null");

      qr =
          jenaApis.query(
              """
              PREFIX kb: <http://knowledgebooks.com/ontology#>
              SELECT ?s ?o WHERE { ?s kb:containsPlace ?o }\
              """);
      System.out.println("qr:" + qr);
      assertNotNull(qr, "Inferred place query result should not be null");

      qr =
          jenaApis.query(
              """
              PREFIX kb: <http://knowledgebooks.com/ontology#>
              SELECT ?o (COUNT(*) AS ?count) WHERE {
                ?s kb:containsPlace ?o
              } GROUP BY ?o\
              """);
      System.out.println("qr:" + qr);
      assertNotNull(qr, "Aggregation query result should not be null");
      assertFalse(qr.getRows().isEmpty(), "Should have aggregated results");
    }
  }
}
