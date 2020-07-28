# Resolve Entity Names to DBPedia References {#ner}

As a personal research project I have collected a large data set that maps entity names (e.g., people's names, city names, names of music groups, company names, etc.) to the DBPedia URI for each entity. I have developed libraries to use this data in [Common Lisp](https://leanpub.com/lovinglisp), [Haskell](https://leanpub.com/haskell-cookbook), and Java. Here we use the Java version of this library.

The Java library is found in the directory **ner_dbpedia** in the GitHub repository. The raw data for these entity to URI mappings are found in the directory **ner_dbpedia/dbpedia_as_text**.

This example shows the use of a standard Java and Maven packaging technique: building a JAR file that contains resource files in addition to compiled Java code. The example code reads the required data resources from the JAR file (or the temporary **target** directory during development). This makes the JAR file self contained when we use this example library in later chapters.


## DBPedia Entities

DBPedia is the structured RDF database that is automatically created from WikiPedia info boxes. We will go into some detail on RDF data in the later chapter [Semantic Web](#semantic-web). The raw data for these entity to URI mappings is found in the directory **ner_dbpedia/dbpedia_as_text** files have the format (for people in this case):

{linenos=off}
~~~~~~~~
Al Stewart      <http://dbpedia.org/resource/Al_Stewart>
Alan Watts      <http://dbpedia.org/resource/Alan_Watts>
~~~~~~~~

If you visit any or these URIs using a web browser, for example [http://dbpedia.org/page/Al_Stewart](http://dbpedia.org/page/Al_Stewart) you will see the DBPedia data for the entity formatted for human reading but to be clear the primary purpose of information in DBPedia is for use by software, not humans.

There are 58953 entities defined with their DBPedia URI and the following listing shows the breakdown of number of entities by entity type by counting the number of lines in each resource file:

{linenos=off}
~~~~~~~~
ner_dbpedia: $ wc -l ./src/main/resources/*.txt
     108 ./src/main/resources/BroadcastNetworkNamesDbPedia.txt
    2580 ./src/main/resources/CityNamesDbpedia.txt
    1786 ./src/main/resources/CompanyNamesDbPedia.txt
     167 ./src/main/resources/CountryNamesDbpedia.txt
   14315 ./src/main/resources/MusicGroupNamesDbPedia.txt
   35606 ./src/main/resources/PeopleDbPedia.txt
     555 ./src/main/resources/PoliticalPartyNamesDbPedia.txt
     351 ./src/main/resources/TradeUnionNamesDbPedia.txt
    3485 ./src/main/resources/UniversityNamesDbPedia.txt
   58953 total
~~~~~~~~

The URI for each entity defines a unique identifier for real world entities as well as concepts. 
## Library Implementation

The following UML class diagram shows the APIs and fields for the two classes in the package **com.markwatson.ner_dbpedia** for this example: **NerMaps** and **TextToDbpediaUris**:

![Overview of Java Class UML Diagram for this Example](images/nerdbpedia-uml.png)

As you see in the following figure showing the IntelliJ Community Edition project for this example, there are nine text files, one for each entity type in the directory **src/main/resources**. Later we will look at the code required to read these files in two cases:

- During development these files are read from **target/classes**.
- During client application use of the JAR file (created using *mvn install*) these files are read as resources from the Java class loader.

![IDE View of Project](images/nerdbpedia-ide.png)

The class **com.markwatson.ner_dbpedia.NerMaps** is a utility for reading the raw entity mapping data files and creating hash tables for these mappings:

{lang="java",linenos=off}
~~~~~~~~
package com.markwatson.ner_dbpedia;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Copyright Mark Watson 2020. Apache 2 license,
 */
public class NerMaps {

  private static String enforceAngleBrackets(String s) {
    if (s.startsWith("<")) return s;
    return "<" + s + ">";
  }
  private static Map<String, String> textFileToMap(String nerFileName) {
    Map<String, String> ret = new HashMap<String, String>();
    try {
      InputStream in = ClassLoader.getSystemResourceAsStream(nerFileName);
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      List<String> lines = new ArrayList<String>();
      String line2;
      while((line2 = reader.readLine()) != null) {
        lines.add(line2);
      }
      reader.close();
      lines.forEach(line -> {
        String[] tokens = line.split("\t");
        if (tokens.length > 1) {
          ret.put(tokens[0], enforceAngleBrackets(tokens[1]));
        }
      });
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return ret;
  }

  static public final Map<String, String> broadcastNetworks = 
    textFileToMap("BroadcastNetworkNamesDbPedia.txt");
  static public final Map<String, String> cityNames = 
    textFileToMap("CityNamesDbpedia.txt");
  static public final Map<String, String> companyames = 
    textFileToMap("CompanyNamesDbPedia.txt");
  static public final Map<String, String> countryNames = 
    textFileToMap("CountryNamesDbpedia.txt");
  static public final Map<String, String> musicGroupNames = 
    textFileToMap("MusicGroupNamesDbPedia.txt");
  static public final Map<String, String> personNames = 
    textFileToMap("PeopleDbPedia.txt");
  static public final Map<String, String> politicalPartyNames = 
    textFileToMap("PoliticalPartyNamesDbPedia.txt");
  static public final Map<String, String> tradeUnionNames = 
    textFileToMap("TradeUnionNamesDbPedia.txt");
  static public final Map<String, String> universityNames = 
    textFileToMap("UniversityNamesDbPedia.txt");
}
~~~~~~~~


The class **com.markwatson.ner_dbpedia.TextToDbpediaUris** processes an input string and uses public fields to output found entity names and matching DBPedia URIs. We will use this code later in the chapter *Automatically Generating Data for Knowledge Graphs*.

The code in the class **TextToDbpediaUris** is simple and repeats two common patterns for each entity type. We will look at some of the code here.

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

The empty constructor is private since it makes no sense to create an instance of **TextToDbpediaUris** without text input. The code supports nine entity types. Here we show the definition of public output fields for just two entity types (people and companies).

As a matter of programming style I generally no longer use getter and setter methods, preferring a more concise coding style. I usually make output fields package default visibility (i.e., no **private** or **public** specification so the fields are public within a package and private from other packages). Here I make them public because the package **nerdbpedia** developed here is meant to be used by other packages. If you prefer using getter and setter methods, modern IDEs like IntelliJ and Eclipse can generate those for you for the example code in this book.

We will handle entity names comprised of one, two, and three word sequences. We check for longer word sequences before shorter sequences:
 
{lang="java",linenos=off}
~~~~~~~~
  public TextToDbpediaUris(String text) {
    String[] tokens = tokenize(text + " . . .");
    String uri = "";
    for (int i = 0, size = tokens.length - 2; i < size; i++) {
      String n2gram = tokens[i] + " " + tokens[i + 1];
      String n3gram = n2gram + " " + tokens[i + 2];
      // check for 3grams:
      if ((uri = NerMaps.personNames.get(n3gram)) != null) {
        log("person", i, i + 2, n3gram, uri);
        i += 2;
        continue;
      }
 ~~~~~~~~

The class **NerMaps** that we previously saw listed converts text files of entities to DBPedia URIs mappings to Java hash maps. The method **log** does two things:

- Prints out the entity type, the word indices from the original tokenized text, the entity name as a single string (combine tokens for an entity to a string), and the DBPedia URI.
- Saves entity mapping in the public fields **personUris**, **personNames**, etc.

After we check for three word entity names, we process two word names, and one word names. Here is an example:

{lang="java",linenos=off}
~~~~~~~~
      // check for 2grams:
      if ((s = NerMaps.personNames.get(n2gram)) != null) {
        log("person", i, i + 1, n2gram, s);
        i += 1;
        continue;
      }
~~~~~~~~

The following listing shows the **log** method that write descriptive output and saves entity mappings. We only show the code for the entity type *person*:

{lang="java",linenos=off}
~~~~~~~~
  public void log(String nerType, int index1, int index2, String ngram, String uri) {
    System.out.println(nerType + "\t" + index1 + "\t" + index2 + "\t" + 
                       ngram + "\t" + uri);
    if (!uri.startsWith("<")) uri = "<" + uri + ">";
    if (nerType.equals("person")) {
      if (!personUris.contains(uri)) {
        personUris.add(uri);
        personNames.add(ngram);
      }
    }
~~~~~~~~

For some NLP applications I will use a standard tokenizer like the OpenNLP tokenizer that we used in two previous chapters. Here, I simply add spaces around punctuation characters and use the Java string **split** method:

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

The following listing shows the code snippet from the unit test code in the class **TextToDbpediaUrisTest** that calls the **TextToDbpediaUris** constructor with a text sample (**junit** boilerplate code is not shown):

{lang="java",linenos=on}
~~~~~~~~
package com.markwatson.ner_dbpedia;
 
  ...

  /**
   * Test that is just for side effect printouts:
   */
  public void test1() throws Exception {
    String s = "PTL Satellite Network covered President Bill Clinton going to "   
      + " Guatemala and visiting the Coca Cola Company.";
    TextToDbpediaUris test = new TextToDbpediaUris(s);
  }
}
~~~~~~~~

On line 11, the object **test** contains public fields for accessing the entity names and corresponding URIs. We will use these fields in the later chapters [Automatically Generating Data for Knowledge Graphs](#kgcreator) and [Knowledge Graph Navigator](#kgn).

Here is the output from running the unit test code:

{linenos=off}
~~~~~~~~
broadcastNetwork 0 2 PTL Satellite Network <http://dbpedia.org/resource/PTL_Satellite_Network>
person	 5	 6	 Bill Clinton	<http://dbpedia.org/resource/Bill_Clinton>
country	 9 10  Guatemala	 <http://dbpedia.org/resource/Guatemala>
company	13	 14  Coca Cola	<http://dbpedia.org/resource/Coca-Cola>
~~~~~~~~

## Wrap-up for Resolving Entity Names to DBPedia References

The idea behind this example is simple but useful for information processing applications using raw text input. We will use this library later in two semantic web examples.
