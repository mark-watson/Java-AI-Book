# Knowledge Graph Navigator {#kgn}


The Knowledge Graph Navigator (which I will often refer to as KGN) is a tool for processing a set of entity names and automatically exploring the public Knowledge Graph [DBPedia](http://dbpedia.org) using SPARQL queries. I started to write KGN for my own use, to automate some things I used to do manually when exploring Knowledge Graphs, and later thought that KGN might be useful also for educational purposes. KGN shows the user the auto-generated SPARQL queries so hopefully the user will learn by seeing examples. KGN uses NLP code developed in earlier chapters and we will reuse that code with a short review of using the APIs.

I have a [web site devoted to different versions of KGN](http://www.knowledgegraphnavigator.com/) that you might find interesting. The most full featured version of KGN, including a full user interface, is featured in my book [Loving Common Lisp, or the Savvy Programmer's Secret Weapon](https://leanpub.com/lovinglisp) that you can read for free online. That version performs more speculative SPARQL queries to find information compared to the example here that I designed for ease of understanding and modification.

I chose to use DBPedia instead of WikiData for this example because DBPedia URIs are human readable. The following URIs represent the concept of a *person*. The semantic meanings of DBPedia and FOAF (friend of a friend) URIs are self-evident to a human reader while the WikiData URI is not:

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

After looking an interactive session using the example program for this chapter (that also includes listing automatically generated SPARQL queries) we will look at the implementation.

## Entity Types Handled by KGN

To keep this example simple we only handle just four entity types:

- People
- Companies
- Cities
- Countries
 
 The entity detection library that we use from an earlier chapter also supports the following entity types that we don't use here:

- Broadcast Networks
- Music Groups
- Political Parties
- Trade Unions
- Universities

In addition to finding detailed information for people, companies, cities, and countries we will also search for relationships between person entities and company entities.

As we look at the KGN implementation I will point out where and how you can easily add support for more entity types and in the wrap-up I will suggest further projects that you might want to try implementing with this example.

## General Design of the KGN Example with Example Output

The example application works by first having the user enter names of people and companies. Using libraries written in two previous chapters, we find entities in the user's input text, and generate SPARQL queries to DBPedia to find information about the entities and relationships between them.

We will start with looking at sample output so you have some understanding on what this implementation of KGN will and will not do. Here is the console output for the example query *"Bill Gates, Melinda Gates and Steve Jobs at Apple Computer, IBM and Microsoft"* (with some output removed for brevity). As you remember from the chapter *Semantic Web*, SPAQRL query results are expressed in class **QueryResult** that contains the variables (labelled as **vars**) in a query and a list of rows (one query result per row). Starting at line 117 in the following listing we see discovered relationships between entities in the input query.

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

Individual People:

  Bill Gates                : http://dbpedia.org/resource/Bill_Gates
[QueryResult vars:[birthplace, label, comment, almamater, spouse]
Rows:
  [http://dbpedia.org/resource/Seattle, Bill Gates, William Henry \"Bill\" Gates III (born October 28, 1955) is an American business magnate, investor, author and philanthropist. In 1975, Gates and Paul Allen co-founded Microsoft, which became the world's largest PC software company. During his career at Microsoft, Gates held the positions of chairman, CEO and chief software architect, and was the largest individual shareholder until May 2014. Gates has authored and co-authored several books., http://dbpedia.org/resource/Harvard_University, http://dbpedia.org/resource/Melinda_Gates]

  Melinda Gates             : http://dbpedia.org/resource/Melinda_Gates
[QueryResult vars:[birthplace, label, comment, almamater, spouse]
Rows:
  [http://dbpedia.org/resource/Dallas | http://dbpedia.org/resource/Dallas,_Texas, Melinda Gates, Melinda Ann Gates (née French; born August 15, 1964), DBE is an American businesswoman and philanthropist. She is co-founder of the Bill & Melinda Gates Foundation. She worked at Microsoft, where she was project manager for Microsoft Bob, Microsoft Encarta and Expedia., http://dbpedia.org/resource/Duke_University, http://dbpedia.org/resource/Bill_Gates]

  Steve Jobs                : http://dbpedia.org/resource/Steve_Jobs
[QueryResult vars:[birthplace, label, comment, almamater, spouse]
Rows:
  [http://dbpedia.org/resource/San_Francisco, Steve Jobs, Steven Paul \"Steve\" Jobs (/ˈdʒɒbz/; February 24, 1955 – October 5, 2011) was an American information technology entrepreneur and inventor. He was the co-founder, chairman, and chief executive officer (CEO) of Apple Inc.; CEO and majority shareholder of Pixar Animation Studios; a member of The Walt Disney Company's board of directors following its acquisition of Pixar; and founder, chairman, and CEO of NeXT Inc. Jobs is widely recognized as a pioneer of the microcomputer revolution of the 1970s and 1980s, along with Apple co-founder Steve Wozniak. Shortly after his death, Jobs's official biographer, Walter Isaacson, described him as a \"creative entrepreneur whose passion for perfection and ferocious drive revolutionized six industries: personal computers, animated movies, music, phones, tab, http://dbpedia.org/resource/Reed_College, http://dbpedia.org/resource/Laurene_Powell_Jobs]


Individual Companies:

  Apple Computer            : http://dbpedia.org/resource/Apple_Inc.
[QueryResult vars:[industry, netIncome, label, comment, numberOfEmployees]
Rows:
  [http://dbpedia.org/resource/Computer_hardware | http://dbpedia.org/resource/Computer_software | http://dbpedia.org/resource/Consumer_electronics | http://dbpedia.org/resource/Corporate_Venture_Capital | http://dbpedia.org/resource/Digital_distribution | http://dbpedia.org/resource/Fabless_manufacturing, 5.3394E10, Apple Inc., Apple Inc. is an American multinational technology company headquartered in Cupertino, California, that designs, develops, and sells consumer electronics, computer software, and online services. Its hardware products include the iPhone smartphone, the iPad tablet computer, the Mac personal computer, the iPod portable media player, the Apple Watch smartwatch, and the Apple TV digital media player. Apple's consumer software includes the macOS and iOS operating systems, the iTunes media player, the Safari web browser, and the iLife and iWork creativity and productivity suites. Its online services include the iTunes Store, the iOS App Store and Mac App Store, Apple Music, and iCloud., 115000]

  IBM                       : http://dbpedia.org/resource/IBM
[QueryResult vars:[industry, netIncome, label, comment, numberOfEmployees]
Rows:
  [http://dbpedia.org/resource/Cloud_computing | http://dbpedia.org/resource/Cognitive_computing | http://dbpedia.org/resource/Information_technology, 1.319E10, IBM, International Business Machines Corporation (commonly referred to as IBM) is an American multinational technology company headquartered in Armonk, New York, United States, with operations in over 170 countries. The company originated in 1911 as the Computing-Tabulating-Recording Company (CTR) and was renamed \"International Business Machines\" in 1924., 377757]

  Microsoft                 : http://dbpedia.org/resource/Microsoft
[QueryResult vars:[industry, netIncome, label, comment, numberOfEmployees]
Rows:
  [http://dbpedia.org/resource/Computer_hardware | http://dbpedia.org/resource/Consumer_electronics | http://dbpedia.org/resource/Digital_distribution | http://dbpedia.org/resource/Software, , Microsoft, Microsoft Corporation /ˈmaɪkrəˌsɒft, -roʊ-, -ˌsɔːft/ (commonly referred to as Microsoft or MS) is an American multinational technology company headquartered in Redmond, Washington, that develops, manufactures, licenses, supports and sells computer software, consumer electronics and personal computers and services. Its best known software products are the Microsoft Windows line of operating systems, Microsoft Office office suite, and Internet Explorer and Edge web browsers. Its flagship hardware products are the Xbox video game consoles and the Microsoft Surface tablet lineup. As of 2011, it was the world's largest software maker by revenue, and one of the world's most valuable companies., 114000]


Individual Cities:

  Seattle                   : http://dbpedia.org/resource/Seattle
[QueryResult vars:[latitude_longitude, populationDensity, label, comment, country]
Rows:
  [POINT(-122.33305358887 47.609722137451), 3150.979715864901, Seattle | Сиэтл | سياتل | シアトル | 西雅圖, Seattle (/siˈætəl/) is a West Coast seaport city and the seat of King County, Washington. With an estimated 684,451 residents as of 2015, Seattle is the largest city in both the state of Washington and the Pacific Northwest region of North America. As of 2015, it is estimated to be the 18th largest city in the United States. In July 2013, it was the fastest-growing major city in the United States and remained in the Top 5 in May 2015 with an annual growth rate of 2.1%. The Seattle metropolitan area is the 15th largest metropolitan area in the United States with over 3.7 million inhabitants. The city is situated on an isthmus between Puget Sound (an inlet of the Pacific Ocean) and Lake Washington, about 100 miles (160 km) south of the Canada–United States border. A major gateway for trade w, ]

Individual Countries:


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

Since the DBPedia queries are time consuming, we use the caching layer from the earlier chapter *Semantic Web* when making SPARQL queries to DBPedia. The cache is especially helpful during development when the same queries are repeatedly used for testing.

The KGN user interface loop allows you to enter queries and see the results. There are two special options that you can enter instead of a query:

- sparql - this will print out all SPARQL queries used to present results. After entering this command the buffer of previous SPARQL queries is emptied. This option is useful for learning SPARQL and you might try pasting a few into the input field for the [public DBPedia SPARQL web app](http://dbpedia.org/sparql) and modifying them.
- demo - this will randomly choose a sample query.


## UML Class Diagram for Example Application

The following UML Class Diagram for KGN shows you an overview of the Java classes we use and their public methods and fields.

![UML Class Diagram for KGN Example Application](images/kgn-uml.png)


## Implementation

We will walk through the classes in the UML Class Diagram for KGN in alphabetical order except we will look at the main program in **KGN.java** last.

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


The class **EntityDetail** defines SPARQL query templates in lines ??-?? TBD that have slots for the URI of the entity URI. We use different templates for different entity types.

We mentioned the **OPTIONAL** triple matching patterns in the chapter *Semantic Web*. Before looking at the Java code, let's first look at how optional matching works. We will run the KGN application asking for information on the city Seattle and then use the **sparql** command to print the generated SPARQL produced by the method **cityResults** (most output is not shown here for brevity):

{lang="sparql",linenos=on}
~~~~~~~~
Enter entities query:
Seattle

Individual Cities:

  Seattle                   : http://dbpedia.org/resource/Seattle
[QueryResult vars:[latitude_longitude, populationDensity, label, comment, country]
Rows:
  [POINT(-122.33305358887 47.609722137451), 3150.979715864901, Seattle | Сиэтл | سياتل | シアトル | 西雅圖, Seattle (/siˈætəl/) is a West Coast seaport city and the seat of King County, Washington. With an estimated 684,451 residents as of 2015, Seattle is the largest city in both the state of Washington and the Pacific Northwest region of North America. As of 2015, it is estimated to be the 18th largest city in the United States. In July 2013, it was the fastest-growing major city in the United States and remained in the Top 5 in May 2015 with an annual growth rate of 2.1%. The Seattle metropolitan area is the 15th largest metropolitan area in the United States with over 3.7 million inhabitants. The city is situated on an isthmus between Puget Sound (an inlet of the Pacific Ocean) and Lake Washington, about 100 miles (160 km) south of the Canada–United States border. A major gateway for trade w, ]

Processing query:
sparql

Generated SPARQL used to get current results:

SELECT DISTINCT
    (GROUP_CONCAT (DISTINCT ?latitude_longitude2; SEPARATOR=' | ') 
        AS ?latitude_longitude) 
    (GROUP_CONCAT (DISTINCT ?populationDensity2; SEPARATOR=' | ') AS ?populationDensity) 
    (GROUP_CONCAT (DISTINCT ?label2; SEPARATOR=' | ') AS ?label) 
    (GROUP_CONCAT (DISTINCT ?comment2; SEPARATOR=' | ') AS ?comment) 
    (GROUP_CONCAT (DISTINCT ?country2; SEPARATOR=' | ') AS ?country) { 
 <http://dbpedia.org/resource/Seattle> <http://www.w3.org/2000/01/rdf-schema#comment>  ?comment2 . FILTER  (lang(?comment2) = 'en') . 
 OPTIONAL { <http://dbpedia.org/resource/Seattle> <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?latitude_longitude2 } . 
 OPTIONAL { <http://dbpedia.org/resource/Seattle> <http://dbpedia.org/ontology/PopulatedPlace/populationDensity> ?populationDensity2 } . 
 OPTIONAL { <http://dbpedia.org/resource/Seattle> <http://dbpedia.org/ontology/country> ?country2 } . 
 OPTIONAL { <http://dbpedia.org/resource/Seattle> <http://www.w3.org/2000/01/rdf-schema#label> ?label2 . } 
 } LIMIT 30
~~~~~~~~

In lines ?-?? TBD, we are trying to find a triple stating which country Seattle is in. There is no triple matching the following statement in the DBPedia knowledge base so the variable **country2** is not bound and the query returns no results for the variable **country**:

{lang="sparql",linenos=off}
~~~~~~~~
<http://dbpedia.org/resource/Seattle> <http://dbpedia.org/ontology/country> ?country2
~~~~~~~~

Notice also the syntax for **GROUP_CONCAT**, for example:

{lang="sparql",linenos=off}
~~~~~~~~
  (GROUP_CONCAT (DISTINCT ?country2; SEPARATOR=' | ') AS ?country)
~~~~~~~~

This collects all values assigned to the binding variable **?country2** into a string value using the separator string " | ". Using **DISTINCT** with **GROUP_CONCAT** conveniently discards duplicate bindings for the variable **?country2**.


{lang="java",linenos=on}
~~~~~~~~
package com.knowledgegraphnavigator;

import com.markwatson.semanticweb.QueryResult;

import java.sql.SQLException; // Cache layer in JenaApis library throws this

public class EntityDetail {

  static public QueryResult genericResults(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    String query =
        String.format(
            "select distinct ?p ?o where { %s ?p ?o . " +
            "  FILTER (!regex(str(?p), 'wiki', 'i')) . " +
            "  FILTER (!regex(str(?p), 'wiki', 'i')) } limit 10",
            entityUri);
    return endpoint.query(query);
  }

  static public String genericAsString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = genericResults(endpoint, entityUri);
    return qr.toString();
  }

  static public QueryResult cityResults(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    String query =
        String.format(cityTemplate, entityUri, entityUri, entityUri,
                      entityUri, entityUri);
    return endpoint.query(query);
  }

  static public String cityAsString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = cityResults(endpoint, entityUri);
    return qr.toString();
  }

  static public QueryResult countryResults(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    String query =
        String.format(countryTemplate, entityUri, entityUri, entityUri,
                      entityUri, entityUri);
    return endpoint.query(query);
  }

  static public String countryAsString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = countryResults(endpoint, entityUri);
    return qr.toString();
  }
  static public QueryResult personResults(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    String query =
        String.format(personTemplate, entityUri, entityUri, entityUri,
                      entityUri, entityUri);
    return endpoint.query(query);
  }

  static public String personAsString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = personResults(endpoint, entityUri);
    return qr.toString();
  }
  static public QueryResult companyResults(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    String query =
        String.format(companyTemplate, entityUri, entityUri, entityUri,
                      entityUri, entityUri);
    return endpoint.query(query);
  }

  static public String companyAsString(Sparql endpoint, String entityUri)
      throws SQLException, ClassNotFoundException {
    QueryResult qr = companyResults(endpoint, entityUri);
    return qr.toString();
  }

  static private String companyTemplate =
    "SELECT DISTINCT" +
    "    (GROUP_CONCAT (DISTINCT ?industry2; SEPARATOR=' | ') AS ?industry)\n" +
    "    (GROUP_CONCAT (DISTINCT ?netIncome2; SEPARATOR=' | ') AS ?netIncome)\n" +
    "    (GROUP_CONCAT (DISTINCT ?label2; SEPARATOR=' | ') AS ?label)\n" +
    "    (GROUP_CONCAT (DISTINCT ?comment2; SEPARATOR=' | ') AS ?comment)\n" +
    "    (GROUP_CONCAT (DISTINCT ?numberOfEmployees2; SEPARATOR=' | ')\n" +
    "         AS ?numberOfEmployees) {\n" +
    "  %s <http://www.w3.org/2000/01/rdf-schema#comment>  ?comment2 .\n" +
    "            FILTER  (lang(?comment2) = 'en') .\n" +
    "  OPTIONAL { %s <http://dbpedia.org/ontology/industry> ?industry2 } .\n" +
    "  OPTIONAL { %s <http://dbpedia.org/ontology/netIncome> ?netIncome2 } .\n" +
    "  OPTIONAL {\n" +
    "    %s <http://dbpedia.org/ontology/numberOfEmployees> ?numberOfEmployees2\n" +
    "  } .\n" +
    "  OPTIONAL { %s <http://www.w3.org/2000/01/rdf-schema#label> ?label2 .\n" +
    "            FILTER (lang(?label2) = 'en') } \n" +
    "} LIMIT 30";
  
  static private String personTemplate =
    "SELECT DISTINCT\n" +
    "    (GROUP_CONCAT (DISTINCT ?birthplace2; SEPARATOR=' | ') AS ?birthplace)  \n" +
    "    (GROUP_CONCAT (DISTINCT ?label2; SEPARATOR=' | ') AS ?label)  \n" +
    "    (GROUP_CONCAT (DISTINCT ?comment2; SEPARATOR=' | ') AS ?comment)  \n" +
    "    (GROUP_CONCAT (DISTINCT ?almamater2; SEPARATOR=' | ') AS ?almamater)  \n" +
    "    (GROUP_CONCAT (DISTINCT ?spouse2; SEPARATOR=' | ') AS ?spouse) {  \n" +
    " %s <http://www.w3.org/2000/01/rdf-schema#comment>  ?comment2 .\n" +
    " FILTER  (lang(?comment2) = 'en') .  \n" +
    " OPTIONAL { %s <http://dbpedia.org/ontology/birthPlace> ?birthplace2 } .  \n" +
    " OPTIONAL { %s <http://dbpedia.org/ontology/almaMater> ?almamater2 } .  \n" +
    " OPTIONAL { %s <http://dbpedia.org/ontology/spouse> ?spouse2 } .  \n" +
    " OPTIONAL { %s  <http://www.w3.org/2000/01/rdf-schema#label> ?label2 . \n" +
    "    FILTER  (lang(?label2) = 'en') }  \n" +
    " } LIMIT 10";

  static private String countryTemplate =
   "SELECT DISTINCT" +
   "   (GROUP_CONCAT (DISTINCT ?areaTotal2; SEPARATOR=' | ') AS ?areaTotal)\n" +
   "   (GROUP_CONCAT (DISTINCT ?label2; SEPARATOR=' | ') AS ?label)\n" +
   "   (GROUP_CONCAT (DISTINCT ?comment2; SEPARATOR=' | ') AS ?comment)\n" +
   "   (GROUP_CONCAT (DISTINCT ?populationDensity2; SEPARATOR=' | ')\n" +
   "     AS ?populationDensity) {\n" +
   "  %s <http://www.w3.org/2000/01/rdf-schema#comment>  ?comment2 .\n" +
   "                           FILTER  (lang(?comment2) = 'en') .\n" +
   "      OPTIONAL { %s <http://dbpedia.org/ontology/areaTotal> ?areaTotal2 } .\n" +
   "      OPTIONAL {\n" +
   "       %s <http://dbpedia.org/ontology/populationDensity> ?populationDensity2\n" +
   "      } .\n" +
   "      OPTIONAL { %s <http://www.w3.org/2000/01/rdf-schema#label> ?label2 . }\n" +
   "} LIMIT 30";

  static private String cityTemplate =
    "SELECT DISTINCT\n" +
    "    (GROUP_CONCAT (DISTINCT ?latitude_longitude2; SEPARATOR=' | ') \n" +
    "              AS ?latitude_longitude) \n" +
    "    (GROUP_CONCAT (DISTINCT ?populationDensity2; SEPARATOR=' | ')\n" +
    "              AS ?populationDensity) \n" +
    "    (GROUP_CONCAT (DISTINCT ?label2; SEPARATOR=' | ') AS ?label) \n" +
    "    (GROUP_CONCAT (DISTINCT ?comment2; SEPARATOR=' | ') AS ?comment) \n" +
    "    (GROUP_CONCAT (DISTINCT ?country2; SEPARATOR=' | ') AS ?country) { \n" +
    " %s <http://www.w3.org/2000/01/rdf-schema#comment>  ?comment2 .\n" +
    "       FILTER  (lang(?comment2) = 'en') . \n" +
    " OPTIONAL {\n" +
    "   %s <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> ?latitude_longitude2\n" +
    " } . \n" +
    " OPTIONAL {\n" +
    "    %s <http://dbpedia.org/ontology/PopulatedPlace/populationDensity> ?populationDensity2\n" +
    " } . \n" +
    " OPTIONAL { %s <http://dbpedia.org/ontology/country> ?country2 } . \n" +
    " OPTIONAL { %s <http://www.w3.org/2000/01/rdf-schema#label> ?label2 . } \n" +
    "} LIMIT 30\n";
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
        String.format(
         "select ?p where { %s ?p %s . "  +
         "   FILTER (!regex(str(?p), 'wikiPage', 'i')) } limit 10",
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
  public static final String RESET  = "\u001B[0m"; // ANSI characters for styling
  public static final String GREEN  = "\u001B[32m";
  public static final String YELLOW = "\u001B[33m";
  public static final String PURPLE = "\u001B[35m";
  public static final String CYAN   = "\u001B[36m";

  private PrintEntityResearchResults() { }

  public PrintEntityResearchResults(Sparql endpoint,
                                    List<EntityAndDescription> people,
                                    List<EntityAndDescription> companies,
                                    List<EntityAndDescription> cities,
                                    List<EntityAndDescription> countries)
      throws SQLException, ClassNotFoundException {
    out("\n" + GREEN + "Individual People:\n" + RESET);
    for (EntityAndDescription person : people) {
      out("  " + GREEN + String.format("%-25s", person.entityName) +
          PURPLE + " : " + removeBrackets(person.entityUri) + RESET);
      out(EntityDetail.personAsString(endpoint, person.entityUri));
    }
    out("\n" + CYAN + "Individual Companies:\n" + RESET);
    for (EntityAndDescription company : companies) {
      out("  " + CYAN + String.format("%-25s", company.entityName) +
          YELLOW + " : " + removeBrackets(company.entityUri) + RESET);
      out(EntityDetail.companyAsString(endpoint, company.entityUri));
    }
    out("\n" + GREEN + "Individual Cities:\n" + RESET);
    for (EntityAndDescription city : cities) {
      out("  " + GREEN + String.format("%-25s", city.entityName) +
          PURPLE + " : " + removeBrackets(city.entityUri) + RESET);
      out(EntityDetail.cityAsString(endpoint, city.entityUri));
    }
    out("\n" + GREEN + "Individual Countries:\n" + RESET);
    for (EntityAndDescription country : countries) {
      out("  " + GREEN + String.format("%-25s", country.entityName) +
          PURPLE + " : " + removeBrackets(country.entityUri) + RESET);
      out(EntityDetail.countryAsString(endpoint, country.entityUri));
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
  //static private String endpoint =
  //      "https://query.wikidata.org/bigdata/namespace/wdq/sparql";
  static private String endpoint = "https://dbpedia.org/sparql";
  public Sparql() {
    this.jenaApis = new JenaApis();
  }

  public QueryResult query(String sparqlQuery)
         throws SQLException, ClassNotFoundException {
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
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class KGN {

  private static List<String> demosList =
   Arrays.asList(
    "Bill Gates and Melinda Gates worked at Microsoft",
    "IBM opened an office in Canada",
    "Steve Jobs worked at Apple Computer and visited IBM and Microsoft in Seattle");

  public KGN() throws Exception {
    Sparql endpoint = new Sparql();

    while (true) {
      String query = getUserQueryFromConsole();
      out("\nProcessing query:\n" + query + "\n");
      if (query.equalsIgnoreCase("sparql")) {
        out("Generated SPARQL used to get current results:\n");
        out(sparql.toString());
        out("\n");
        clearSparql();
      } else {
        if (query.equalsIgnoreCase("demo")) {
          query = demosList.get((int) (Math.random() * (demosList.size() + 1)));
        }
        TextToDbpediaUris kt = new TextToDbpediaUris(query);
        List<EntityAndDescription> userSelectedPeople = new ArrayList();
        if (kt.personNames.size() > 0) {
          for (int i = 0; i < kt.personNames.size(); i++) {
            userSelectedPeople.add(
                new EntityAndDescription(kt.personNames.get(i),
                                     kt.personUris.get(i)));
          }
        }
        List<EntityAndDescription> userSelectedCompanies = new ArrayList();
        if (kt.companyNames.size() > 0) {
          for (int i = 0; i < kt.companyNames.size(); i++) {
            userSelectedCompanies.add(
                new EntityAndDescription(kt.companyNames.get(i),
                                     kt.companyUris.get(i)));
          }
        }
        List<EntityAndDescription> userSelectedCities = new ArrayList();
        if (kt.cityNames.size() > 0) {
          out("+++++ kt.cityNames:" + kt.cityNames.toString());
          for (int i = 0; i < kt.cityNames.size(); i++) {
            userSelectedCities.add(
                new EntityAndDescription(kt.cityNames.get(i), kt.cityUris.get(i)));
          }
        }
        List<EntityAndDescription> userSelectedCountries = new ArrayList();
        if (kt.countryNames.size() > 0) {
          out("+++++ kt.countryNames:" + kt.countryNames.toString());
          for (int i = 0; i < kt.countryNames.size(); i++) {
            userSelectedCountries.add(
                new EntityAndDescription(kt.countryNames.get(i),
                                     kt.countryUris.get(i)));
          }
        }
        new PrintEntityResearchResults(endpoint,
            userSelectedPeople,
            userSelectedCompanies,
            userSelectedCities,
            userSelectedCountries);

        for (EntityAndDescription person1 : userSelectedPeople) {
          for (EntityAndDescription person2 : userSelectedPeople) {
            if (person1 != person2) {
              QueryResult qr = 
                EntityRelationships.results(endpoint, person1.entityUri,
                                            person2.entityUri);
              if (qr.rows.size() > 0) {
                out("Relationships between person " + person1.entityName +
                    " person " + person2.entityName + ":");
                out(qr.toString());
              }
            }
          }
        }

        for (EntityAndDescription person : userSelectedPeople) {
          for (EntityAndDescription company : userSelectedCompanies) {
            QueryResult qr = 
              EntityRelationships.results(endpoint, person.entityUri,
                                          company.entityUri);
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
              QueryResult qr = 
                EntityRelationships.results(endpoint, company1.entityUri, 
                                            company2.entityUri);
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

  private String getUserQueryFromConsole() {
    out("Enter entities query:");
    Scanner input = new Scanner(System.in);
    String ret = "";
    while (input.hasNext()) {
      ret = input.nextLine();
      break;
    }
    return ret;
  }

  public static void main(String[] args) throws Exception {
    new KGN();
  }
}
~~~~~~~~




## Wrap-up

If you enjoy running and experimenting with this example and want to modify it for your own projects then I hope that I provided a sufficient road map for you to do so.

I suggest further projects that you might want to try implementing with this example:

- TBD
- TBD
- TBD


I got the idea for the KGN application because I was spending quite a bit of time manually setting up SPARQL queries for DBPedia (and other public sources like WikiData) and I wanted to experiment with partially automating this process.

