# Automatically Generating Data for Knowledge Graphs {#kgcreator}

Here we develop a complete application. The Knowledge Graph Creator (KGcreator) is a tool for automating the generation of data for Knowledge Graphs from raw text data. We will see how to create a single standalone executable file using SBCL Common Lisp. The application can also be run during development from a repl. This application also implements a web application interface.

Data created by KGcreator generates data in RDF triples suitable for loading into any linked data/semantic web data store.

This example application works by identifying entities in text. Example entity types are people, companies, country names, city names, broadcast network names, political party names, and university names. We saw earlier code for detecting entities in the chapter on making named entities to DBPedia URIs and we will reuse this code. We will discuss later three strategies for reusing code from different projects.

When I originally wrote KGCreator as two research prototypes, one in Common Lisp (the example in this chapter) and one in Haskell (which I also use as an example in my book [Haskell Tutorial and Cookbook](https://leanpub.com/haskell-cookbook/). The example in this chapter is a port of these systems to Java.

![Part of a Knowledge Graph shown in Neo4j web application console](images/neo4j.jpg)

![Detail of Neo4j console](images/neo4j_ex1.jpg)


## Implementation Notes


TBD

TBD


## Generating RDF Data

RDF data is comprised of triples, where the value for each triple are a subject, a predicate, and an object. Subjects are URIs, predicates are usually URIs, and objects are either literal values or URIs. Here are two triples written by this example application:

{linenos=off}
~~~~~~~~
<http://dbpedia.org/resource/The_Wall_Street_Journal> 
  <http://knowledgebooks.com/schema/aboutCompanyName> 
  "Wall Street Journal" .
<https://newsshop.com/june/z902.html>
  <http://knowledgebooks.com/schema/containsCountryDbPediaLink>
  <http://dbpedia.org/resource/Canada> .
~~~~~~~~


The following listing of the file **  TBD   TBD  TBD  ** generates RDF data:

{lang="java",linenos=on}
~~~~~~~~
~~~~~~~~


This code works on a list of paired files for text data and the meta data for each text file. As an example, if there is an input text file test123.txt then there would be a matching meta file test123.meta that contains the source of the data in the file test123.txt. This data source will be a URI on the web or a local file URI. The top level function ** TBD  TBD rename: rdf-from-files** takes an output file path for writing the generated RDF data and a list of pairs of text and meta file paths.

A global variable **  TBD  TBD rename:  \*rdf-nodes-hash\*** will be used to remember the nodes in the RDF graph as it is generated. Please note that the function **rdf-from-files** is not re-entrant: it uses the global **  TBD  TBD rename:  \*rdf-nodes-hash\*** so if you are writing multi-threaded applications it will not work to execute the function **  TBD  TBD rename: rdf-from-files** simultaneously in multiple threads of execution.

The function **  TBD  TBD rename: rdf-from-files** (and the nested functions) are straightforward. I left a few debug printout statements in the code and when you run the test code that I left in the bottom of the file, hopefully it will be clear what rdf.lisp is doing. 

TBD


## Generating Data for the Neo4j Graph Database

Now we will generate Neo4J Cypher data. In order to keep the implementation simple, both the RDF and Cypher generation code starts with raw text and performs the NLP analysis to find entities. This example could be refactored to perform the NLP analysis just one time but in practice you will likely be working with either RDF or NEO4J and so you will probably extract just the code you need from this example (i.e., either the RDF or Cypher generation code).

Before we look at the code, let's start with a few lines of generated Neo4J Cypher import data:

{linenos=off}
~~~~~~~~
CREATE (newsshop_com_june_z902_html_news)-[:ContainsCompanyDbPediaLink]->(Wall_Street_Journal)
CREATE (Canada:Entity {name:"Canada", uri:"<http://dbpedia.org/resource/Canada>"})
CREATE (newsshop_com_june_z902_html_news)-[:ContainsCountryDbPediaLink]->(Canada)
CREATE (summary_of_abcnews_go_com_US_violent_long_lasting_tornadoes_threaten_oklahoma_texas_storyid63146361:Summary {name:"summary_of_abcnews_go_com_US_violent_long_lasting_tornadoes_threaten_oklahoma_texas_storyid63146361", uri:"<https://abcnews.go.com/US/violent-long-lasting-tornadoes-threaten-oklahoma-texas/story?id=63146361>", summary:"Part of the system that delivered severe weather to the central U.S. over the weekend is moving into the Northeast today, producing strong to severe storms -- damaging winds, hail or isolated tornadoes can't be ruled out. Severe weather is forecast to continue on Tuesday, with the western storm moving east into the Midwest and parts of the mid-Mississippi Valley."})
~~~~~~~~


The following listing of file ** TBD  TBD rename:  src/kgcreator/neo4j.lisp** is similar to the code that generated RDF in the last section:

{lang="java",linenos=on}
~~~~~~~~

~~~~~~~~

You can load all of KGCreator but just execute the test function at the end of this file using:

{lang="java",linenos=on}
~~~~~~~~
~~~~~~~~

## Implementing the Top Level Application APIs

TBD


{lang="java",linenos=on}
~~~~~~~~

~~~~~~~~


## KGCreator Wrap Up

When developing applications or systems using Knowledge Graphs it is useful to be able to quickly generate test data which is the primary purpose of KGCreator. A secondary use is to generate  Knowledge Graphs for production use using text data sources. In this second use case you will want to manually inspect the generated data to verify its correctness or usefulness for your application.