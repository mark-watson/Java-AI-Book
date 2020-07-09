# Knowledge Graph Navigator {#kgn}


The Knowledge Graph Navigator (which I will often refer to as KGN) is a tool for processing a set of entity names and automatically exploring the public Knowledge Graph [DBPedia](http://dbpedia.org) using SPARQL queries. I started to write KGN for my own use, to automate some things I used to do manually when exploring Knowledge Graphs, and later thought that KGN might be useful also for educational purposes. KGN shows the user the auto-generated SPARQL queries so hopefully the user will learn by seeing examples. KGN uses NLP code developed in earlier chapters and we will reuse that code with a short review of using the APIs.

I chose to use DBPedia instead of WikiData for this example because DBPedia URIs are human readable. The following URIs represent the concept of a *person*. The semantic meanings of DBPedia and FOAF (friend of a friend) URIs are self-evident to a human reader.

{linenos=off}
~~~~~~~~
http://www.wikidata.org/entity/Q215627
http://dbpedia.org/ontology/Person
http://xmlns.com/foaf/0.1/name
~~~~~~~~

I frequently use WikiData in my work and WikiData is one of the most useful public knowledge bases. I have both DBPedia and WikiData Sparql endpoints in the file **Sparql.java**, with the WikiData endpoint comment out. You can try manually querying WikiData at the [WikiData SPARL endpoint](https://query.wikidata.org). For example, you might explore the WikiData URI for the *person* concept using:

{lang=sparql, linenos=off}
~~~~~~~~
select ?p ?o where { <http://www.wikidata.org/entity/Q215627> ?p ?o  } limit 10
~~~~~~~~

For the rest of this chapter we will just use DBPedia.

## We handle Person and Company Entity Types

To keep this example simple we only handle two entity types. However, the entity detection library that we use from an earlier chapter also supports, in addition to people and companies:

- Cities
- Countries
- Broadcast Networks
- Music Groups
- Political Parties
- Trade Unions
- Universities

As we look at the KGN implementation I will point out where and how you can easily add support for more entity types and in the wrap-up I will suggest further projects that you might want to try implementing with this example.

## General Design of the KGN Example

After looking an interactive session using the example program for this chapter (that also includes listing automatically generated SPARQL queries) we will look at the implementation.

The example application works by first having the user enter names of people and companies. Using libraries written in two previous chapters, we find entities in the user's input text, and generate SPARQL queries to DBPedia to find information about the entities and relationships between them.

We use Andreas Wegmann's **consoleui** library for showing the user a list of entities, allowing the user to select entities (toggle with the space character), and accept the list of selected entities by entering the return key. The following figure shows a screen shot of processing a list of person entities and selecting the first two using the up/down arrow keys and the space bare to toggle selections on or off:

![Using the ConsoleUI library to select list items](images/kgn_menu.png)

Here is the console output for the example query *"Bill Gates, Melinda Gates and Steve Jobs at Apple Computer, IBM and Microsoft"*:

{linenos=on}
~~~~~~~~
Enter entities query:
Bill Gates, Melinda Gates and Steve Jobs at Apple Computer, IBM and Microsoft

Processing query:
Bill Gates, Melinda Gates and Steve Jobs at Apple Computer, IBM and Microsoft

person	0	1	Bill Gates	<http://dbpedia.org/resource/Bill_Gates>
person	4	5	Melinda Gates	<http://dbpedia.org/resource/Melinda_Gates>
person	7	8	Steve Jobs	<http://dbpedia.org/resource/Steve_Jobs>
company	10	11	Apple Computer	<http://dbpedia.org/resource/Apple_Inc.>
company	14	15	IBM	<http://dbpedia.org/resource/IBM>
company	16	17	Microsoft	<http://dbpedia.org/resource/Microsoft>
? entities [0 Bill Gates, 1 Melinda Gates, 2 Steve Jobs]
? entities [0 Apple Computer, 1 IBM, 2 Microsoft]

Individual People:

  Bill Gates                : http://dbpedia.org/resource/Bill_Gates
[QueryResult vars:[p, o]
Rows:
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/2002/07/owl#Thing]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://dbpedia.org/ontology/Person]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Agent]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#NaturalPerson]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.wikidata.org/entity/Q215627]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.wikidata.org/entity/Q24229398]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.wikidata.org/entity/Q5]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://dbpedia.org/ontology/Agent]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://schema.org/Person]

  Melinda Gates             : http://dbpedia.org/resource/Melinda_Gates
[QueryResult vars:[p, o]
Rows:
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/2002/07/owl#Thing]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://dbpedia.org/ontology/Person]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Agent]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#NaturalPerson]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.wikidata.org/entity/Q215627]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.wikidata.org/entity/Q24229398]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.wikidata.org/entity/Q5]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://dbpedia.org/ontology/Agent]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://schema.org/Person]

  Steve Jobs                : http://dbpedia.org/resource/Steve_Jobs
[QueryResult vars:[p, o]
Rows:
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.w3.org/2002/07/owl#Thing]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://xmlns.com/foaf/0.1/Person]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://dbpedia.org/ontology/Person]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#Agent]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.ontologydesignpatterns.org/ont/dul/DUL.owl#NaturalPerson]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.wikidata.org/entity/Q215627]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.wikidata.org/entity/Q24229398]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://www.wikidata.org/entity/Q5]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://dbpedia.org/ontology/Agent]
  [http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://schema.org/Person]


Individual Companies:

  Apple Computer            : http://dbpedia.org/resource/Apple_Inc.
  IBM                       : http://dbpedia.org/resource/IBM
  Microsoft                 : http://dbpedia.org/resource/Microsoft

Relationships between person Bill Gates person Melinda Gates:
[QueryResult vars:[p]
Rows:
  [http://dbpedia.org/ontology/spouse]

Relationships between person Melinda Gates person Bill Gates:
[QueryResult vars:[p]
Rows:
  [http://dbpedia.org/ontology/spouse]

Relationships between person Bill Gates company Microsoft:
[QueryResult vars:[p]
Rows:
  [http://dbpedia.org/ontology/board]

Relationships between person Steve Jobs company Apple Computer:
[QueryResult vars:[p]
Rows:
  [http://www.w3.org/2000/01/rdf-schema#seeAlso]
  [http://dbpedia.org/ontology/board]
  [http://dbpedia.org/ontology/occupation]
~~~~~~~~

Since the DBPedia queries are time consuming, we use the caching layer from the earlier chapter *Resolve Entity Names to DBPedia References*. The cache is especially helpful during development when the same queries are repeatedly used for testing.

## UML Class Diagram for Example Application

TBD


![UML Class Diagram for KGN Example Application](images/kgn-uml.png)


## Implementation


{lang="java",linenos=on}
~~~~~~~~
package com.knowledgegraphnavigator;

// uses https://github.com/awegmann/consoleui by Andreas Wegmann (Apache 2 license) - see pom.xml

import static com.knowledgegraphnavigator.Log.out;

import de.codeshelf.consoleui.elements.PromptableElementIF;
import de.codeshelf.consoleui.elements.items.CheckboxItemIF;
import de.codeshelf.consoleui.prompt.CheckboxResult;
import de.codeshelf.consoleui.prompt.ConsolePrompt;
import de.codeshelf.consoleui.prompt.PromtResultItemIF;
import de.codeshelf.consoleui.prompt.builder.CheckboxPromptBuilder;
import de.codeshelf.consoleui.prompt.builder.PromptBuilder;
import jline.TerminalFactory;

import java.util.*;

public class ConsoleUserInterface {
  public ConsoleUserInterface() {
  }


  public String getUserQueryFromConsole() {
    out("Enter entities query:");
    Scanner input = new Scanner(System.in);
    String ret = "";
    while (input.hasNext()) {
      ret = input.nextLine();
      break;
    }
    return ret;
  }

  public List<EntityAndDescription> selectUsingCheckBox(List<EntityAndDescription> items) throws Exception {
    List<EntityAndDescription> ret = new ArrayList<>();
    ConsolePrompt prompt = new ConsolePrompt();
    PromptBuilder promptBuilder = prompt.getPromptBuilder();
    List<CheckboxItemIF> list = new ArrayList<CheckboxItemIF>();
    CheckboxPromptBuilder pp = promptBuilder.createCheckboxPrompt()
        .name("entities")
        //.message("Please select 1 or more:")
        .newSeparator("entities")
        .add();
    int count = 0;
    for (EntityAndDescription ead : items) {
      int len = Math.min(70, ead.entityUri.length());
      pp.newItem().name("" + count + " " + ead.entityName).text(ead.entityName + " || " + ead.entityUri.substring(0, len)).add();
      count += 1;
    }
    pp.addPrompt();
    List<PromptableElementIF> promptableElementList = promptBuilder.build();
    HashMap<String, ? extends PromtResultItemIF> ret2 = prompt.prompt(promptableElementList);
    CheckboxResult o1 = (CheckboxResult)ret2.get("entities");
    for (String s : o1.getSelectedIds()) {
      int index = s.indexOf(' ');
      if (index > -1) {
        int i = Integer.parseInt(s.substring(0, index));
        ret.add(items.get(i));
      }
    }
    TerminalFactory.get().restore();
    for (Thread aThread : Thread.getAllStackTraces().keySet()) {
      if (aThread.getName().startsWith("NonBlockingInputStreamThread")) {
        aThread.stop();
      }
    }
    return ret;
  }

  public static void main(String[] args) throws Exception {
    ConsoleUserInterface console = new ConsoleUserInterface();
    String query = console.getUserQueryFromConsole();
    System.out.println("++ query: " + query);
  }
}
~~~~~~~~
                      
This caching layer greatly speeds up my own personal use of KGN. Without caching, queries that contain many entity references simply take too long to run. The UI for the KGN application has a menu option for clearing the local cache but I almost never use this option because growing a large cache that is tailored for the types of information I search for makes the entire system much more responsive.


{lang="java",linenos=on}
~~~~~~~~
package com.knowledgegraphnavigator;

public class EntityAndDescription {
  public String entityName;
  public String entityUri;
  public EntityAndDescription(String entityName, String entityUri) {
    this.entityName = entityName;
    this.entityUri = entityUri;
  }
  public String toString() {
    return "[EntityAndDescription name: " + entityName +
        " description: " + entityUri + "]";
  }
}
~~~~~~~~





{lang="java",linenos=on}
~~~~~~~~
package com.knowledgegraphnavigator;

import com.markwatson.semanticweb.QueryResult;

import java.sql.SQLException;

import static com.knowledgegraphnavigator.Log.out;
import static com.knowledgegraphnavigator.Log.sparql;

public class EntityDetail {

  static public QueryResult results(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    // note: find entity type with a SPARQL query, then  set var names ?p ?o
    // as appropriate for entity type.  TBD

    String query =
        String.format(
            "select distinct ?p ?o where { %s ?p ?o . FILTER (!regex(str(?p), 'wiki', 'i')) " +
                " . FILTER (!regex(str(?p), 'wiki', 'i')) } limit 10",
            entityUri);
    return endpoint.query(query);
  }

  static public String asString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = results(endpoint, entityUri);
    return qr.toString();
  }

}
~~~~~~~~





{lang="java",linenos=on}
~~~~~~~~
package com.knowledgegraphnavigator;

import com.markwatson.semanticweb.QueryResult;

import java.sql.SQLException;

public class EntityRelationships {

  static public QueryResult results(Sparql endpoint,
                                    String entity1Uri, String entity2Uri)
      throws SQLException, ClassNotFoundException {
    String query =
        String.format("select ?p where { %s ?p %s . FILTER (!regex(str(?p), 'wikiPage', 'i')) } limit 10",
            entity1Uri, entity2Uri);
    return endpoint.query(query);
  }
}
~~~~~~~~





{lang="java",linenos=on}
~~~~~~~~
package com.knowledgegraphnavigator;

public class Log {
  static public void out(String s) { System.out.println(s); }
  static public StringBuilder sparql  = new StringBuilder();
  static public void clearSparql() { sparql.delete(0, sparql.length()); }
}
~~~~~~~~





{lang="java",linenos=on}
~~~~~~~~
package com.knowledgegraphnavigator;

import static com.knowledgegraphnavigator.Log.out;
import static com.knowledgegraphnavigator.Utils.removeBrackets;

import java.sql.SQLException;
import java.util.List;

public class PrintEntityResearchResults {

  /**
   * Note for Windows users: the Windows console may not render the following
   * ANSI terminal escape sequences correctly. If yo have problems, just
   * change the following to the empty string "":
   */
  public static final String RESET = "\u001B[0m"; // ANSI characters for styling
  public static final String GREEN = "\u001B[32m";
  public static final String YELLOW = "\u001B[33m";
  public static final String PURPLE = "\u001B[35m";
  public static final String CYAN = "\u001B[36m";

  private PrintEntityResearchResults() { }

  public PrintEntityResearchResults(Sparql endpoint,
                                    List<EntityAndDescription> people,
                                    List<EntityAndDescription> companies)
      throws SQLException, ClassNotFoundException {
    out("\n" + GREEN + "Individual People:\n" + RESET);
    for (EntityAndDescription person : people) {
      out("  " + GREEN + String.format("%-25s", person.entityName) +
          PURPLE + " : " + removeBrackets(person.entityUri) + RESET);
      out(EntityDetail.asString(endpoint, person.entityUri));
    }
    out("\n" + CYAN + "Individual Companies:\n" + RESET);
    for (EntityAndDescription company : companies) {
      out("  " + CYAN + String.format("%-25s", company.entityName) +
          YELLOW + " : " + removeBrackets(company.entityUri) + RESET);
    }
    out("");
  }
}
~~~~~~~~





{lang="java",linenos=on}
~~~~~~~~
package com.knowledgegraphnavigator;

import com.markwatson.semanticweb.QueryResult;
import com.markwatson.semanticweb.JenaApis;
import static com.knowledgegraphnavigator.Log.sparql;
import static com.knowledgegraphnavigator.Log.out;

import java.sql.SQLException;

public class Sparql {
  //static private String endpoint = "https://query.wikidata.org/bigdata/namespace/wdq/sparql";
  static private String endpoint = "https://dbpedia.org/sparql";
  public Sparql() {
    this.jenaApis = new JenaApis();
  }

  public QueryResult query(String sparqlQuery) throws SQLException, ClassNotFoundException {
    //out(sparqlQuery); // debug for now...
    sparql.append(sparqlQuery);
    sparql.append(("\n\n"));
    return jenaApis.queryRemote(endpoint, sparqlQuery);
  }
  private JenaApis jenaApis;

  public static void main(String[] args) throws Exception {
    Sparql sp = new Sparql();
    QueryResult qr = sp.query("select ?s ?p ?o where { ?s ?p ?o } limit 5");
    out(qr.toString());
  }
}
~~~~~~~~



{lang="java",linenos=on}
~~~~~~~~
package com.knowledgegraphnavigator;

public class Utils {
  static public String removeBrackets(String s) {
    if (s.startsWith("<")) return s.substring(1, s.length() - 1);
    return s;
  }
}
~~~~~~~~



Finally we get to the main program:

{lang="java",linenos=on}
~~~~~~~~
package com.knowledgegraphnavigator;

import com.markwatson.ner_dbpedia.TextToDbpediaUris;
import com.markwatson.semanticweb.QueryResult;

import static com.knowledgegraphnavigator.Log.out;
import static com.knowledgegraphnavigator.Log.sparql;
import static com.knowledgegraphnavigator.Log.clearSparql;

import java.util.ArrayList;
import java.util.List;

public class KGN {

  public KGN() throws Exception {
    Sparql endpoint = new Sparql();

    ConsoleUserInterface console = new ConsoleUserInterface();

    while (true) {
      String query = console.getUserQueryFromConsole();
      out("\nProcessing query:\n" + query + "\n");
      if (query.equalsIgnoreCase("sparql")) {
        out("Generated SPARQL used to get current results:\n");
        out(sparql.toString());
        out("\n");
        clearSparql();
      } else {
        TextToDbpediaUris kt = new TextToDbpediaUris(query);
        List<EntityAndDescription> userSelectedPeople = new ArrayList();
        List<EntityAndDescription> userSelectedCompanies = new ArrayList();
        if (kt.personNames.size() > 0) {
          List<EntityAndDescription> entityAndDescriptionList = new ArrayList();
          for (int i = 0; i < kt.personNames.size(); i++) {
            entityAndDescriptionList.add(
                new EntityAndDescription(kt.personNames.get(i), kt.personUris.get(i)));
          }
          userSelectedPeople = console.selectUsingCheckBox(entityAndDescriptionList);
        }
        if (kt.companyNames.size() > 0) {
          List<EntityAndDescription> entityAndDescriptionList = new ArrayList();
          for (int i = 0; i < kt.companyNames.size(); i++) {
            entityAndDescriptionList.add(
                new EntityAndDescription(kt.companyNames.get(i), kt.companyUris.get(i)));
          }
          userSelectedCompanies = console.selectUsingCheckBox(entityAndDescriptionList);
        }
        new PrintEntityResearchResults(endpoint, userSelectedPeople, userSelectedCompanies);

        for (EntityAndDescription person1 : userSelectedPeople) {
          for (EntityAndDescription person2 : userSelectedPeople) {
            if (person1 != person2) {
              QueryResult qr = EntityRelationships.results(endpoint, person1.entityUri, person2.entityUri);
              if (qr.rows.size() > 0) {
                out("Relationships between person " + person1.entityName +
                    " person " + person2.entityName + ":");
                out(qr.toString());
              }
            }
          }
        }
        // for testing: Bill Gates, Melinda Gates and Steve Jobs at Apple Computer, IBM and Microsoft
        for (EntityAndDescription person : userSelectedPeople) {
          for (EntityAndDescription company : userSelectedCompanies) {
            QueryResult qr = EntityRelationships.results(endpoint, person.entityUri, company.entityUri);
            if (qr.rows.size() > 0) {
              out("Relationships between person " + person.entityName +
                  " company " + company.entityName + ":");
              out(qr.toString());
            }
          }
        }
        for (EntityAndDescription company1 : userSelectedCompanies) {
          for (EntityAndDescription company2 : userSelectedCompanies) {
            if (company1 != company2) {
              QueryResult qr = EntityRelationships.results(endpoint, company1.entityUri, company2.entityUri);
              if (qr.rows.size() > 0) {
                out("Relationships between company " + company1.entityName +
                    " company " + company2.entityName + ":");
                out(qr.toString());
              }
            }
          }
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    new KGN();
  }
}
~~~~~~~~



{lang="java",linenos=on}
~~~~~~~~
~~~~~~~~



## Wrap-up

If you enjoy running and experimenting with this example and want to modify it for your own projects then I hope that I provided a sufficient road map for you to do so.

I suggest further projects that you might want to try implementing with this example:

- TBD
- - TBD
- TBD


I got the idea for the KGN application because I was spending quite a bit of time manually setting up SPARQL queries for DBPedia (and other public sources like WikiData) and I wanted to experiment with partially automating this process.

