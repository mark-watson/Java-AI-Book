package com.knowledgegraphnavigator;

import com.markwatson.semanticweb.QueryResult;

import java.sql.SQLException;

public class EntityRelationships {

  public static QueryResult results(Sparql endpoint,
                                    String entity1Uri, String entity2Uri)
      throws SQLException, ClassNotFoundException {
    var query = """
        SELECT ?p WHERE {
          %s ?p %s .
          FILTER (!regex(str(?p), 'wikiPage', 'i'))
        } LIMIT 10
        """.formatted(entity1Uri, entity2Uri);
    return endpoint.query(query);
  }
}
