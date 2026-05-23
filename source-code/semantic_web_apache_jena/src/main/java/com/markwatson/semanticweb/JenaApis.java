package com.markwatson.semanticweb;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class JenaApis implements AutoCloseable {

  public JenaApis() {
    //model = ModelFactory.createDefaultModel(); // use if OWL reasoning not required
    model = ModelFactory.createOntologyModel(); // use OWL reasoner
  }

  public Model model() {
    return model;
  }

  public void loadRdfFile(String fpath) {
    model.read(fpath);
  }

  public void saveModelToTurtleFormat(String outputPath) throws IOException {
    try (var fos = new FileOutputStream(outputPath)) {
      RDFDataMgr.write(fos, model, RDFFormat.TRIG_PRETTY);
    }
  }

  public void saveModelToN3Format(String outputPath) throws IOException {
    try (var fos = new FileOutputStream(outputPath)) {
      RDFDataMgr.write(fos, model, RDFFormat.NTRIPLES);
    }
  }

  public QueryResult query(String sparqlQuery) {
    try (QueryExecution qexec = QueryExecution.model(model)
        .query(sparqlQuery)
        .build()) {
      ResultSet results = qexec.execSelect();
      var qr = new QueryResult(results.getResultVars());
      for (; results.hasNext(); ) {
        QuerySolution solution = results.nextSolution();
        List<String> newResultRow = new ArrayList<>();
        for (String var : qr.variableList) {
          newResultRow.add(solution.get(var).toString());
        }
        qr.rows.add(newResultRow);
      }
      return qr;
    }
  }

  public QueryResult queryRemote(String service, String sparqlQuery) throws SQLException {
    if (cache == null) cache = new Cache();
    byte[] b = cache.fetchResultFromCache(sparqlQuery);
    if (b != null) {
      //System.out.println("Found query in cache.");
      return SerializationUtils.deserialize(b);
    }
    try (QueryExecution qexec = QueryExecution.service(service)
        .query(sparqlQuery)
        .build()) {
      ResultSet results = qexec.execSelect();
      var qr = new QueryResult(results.getResultVars());
      for (; results.hasNext(); ) {
        QuerySolution solution = results.nextSolution();
        List<String> newResultRow = new ArrayList<>();
        for (String var : qr.variableList) {
          newResultRow.add(solution.get(var).toString());
        }
        qr.rows.add(newResultRow);
      }
      byte[] serialized = SerializationUtils.serialize(qr);
      cache.saveQueryResultInCache(sparqlQuery, serialized);
      return qr;
    }
  }

  @Override
  public void close() throws SQLException {
    if (cache != null) {
      cache.close();
    }
  }

  private Cache cache = null;
  private final Model model;

  public static void main(String[] args) {
    /*
    Execute using, for example:
         mvn exec:java -Dexec.mainClass="com.markwatson.semanticweb.JenaApis" \
             -Dexec.args="data/news.n3"
     */
    JenaApis ja = new JenaApis();
    System.out.println(args.length);
    if (args.length == 0) {
      // no RDF input file names on command line so use a default file:
      ja.loadRdfFile("data/news.n3");
    } else {
      for (String fpath : args) {
        ja.loadRdfFile(fpath);
      }
    }
    System.out.println("Multi-line queries are OK but don't use blank lines.");
    System.out.println("Enter a blank line to process query.");
    while (true) {
      System.out.println("Enter a SPARQL query:");
      Scanner sc = new Scanner(System.in);
      StringBuilder sb = new StringBuilder();
      while (sc.hasNextLine()) {  //until no other inputs to proceed
        String s = sc.nextLine();
        if (s.equalsIgnoreCase("quit") || s.equalsIgnoreCase("exit"))
          System.exit(0);
        if (s.isEmpty()) break;
        sb.append(s);
        sb.append("\n");
      }
      QueryResult qr = ja.query(sb.toString());
      System.out.println(qr);
    }
  }
}