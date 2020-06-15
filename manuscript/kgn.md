# Knowledge Graph Navigator {#kgn}


The Knowledge Graph Navigator (which I will often refer to as KGN) is a tool for processing a set of entity names and automatically exploring the public Knowledge Graph [DBPedia](http://dbpedia.org) using SPARQL queries. I started to write KGN for my own use, to automate some things I used to do manually when exploring Knowledge Graphs, and later thought that KGN might be useful also for educational purposes. KGN shows the user the auto-generated SPARQL queries so hopefully the user will learn by seeing examples. KGN uses NLP code developed in earlier chapters and we will reuse that code with a short review of using the APIs.

TBD

After looking at generated SPARQL for an example query use of the application, we will start a process of bottom up development, first writing low level functions to automate SPARQL queries, writing utilities we will need for the UI, and finally writing the UI. Some of the problems we will need to solve along the way will be colorizing the output the user sees in the UI and implementing a progress bar so the application user does not think the application is "hanging" while generating and making SPARQL queries to DBPedia.

Since the DBPedia queries are time consuming, we will also implement a caching layer using SQLite that will make the app more responsive. The cache is especially helpful during development when the same queries are repeatedly used for testing.



## Example Output

Before we get started studying the implementation, let's look at sample output in order to help give meaning to the code we will look at later. Consider a query that a user might type into the top query field in the KGN app:

        Steve Jobs lived near San Francisco and was
        a founder of \<http://dbpedia.org/resource/Apple_Inc.\>

The system will try to recognize entities in a query. If you know the DBPedia URI of an entity, like the company Apple in this example, you can use that directly. Note that in the SPARQL  URIs are surrounded with angle bracket characters.

The application prints out automatically generated SPARQL queries. For the above listed example query the following output will be generated (some editing to fit page width):

{linenos=off}
~~~~~~~~
Trying to get entity by name = Steve Jobs using SPARQL with type:
~~~~~~~~

{lang="sparql",linenos=off}
~~~~~~~~
select distinct ?s ?comment { ?s ?p "Steve Jobs"@en . 
 ?s <http://www.w3.org/2000/01/rdf-schema#comment> ?comment .
   FILTER ( lang ( ?comment ) = 'en' ) . 
 ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
    <http://dbpedia.org/ontology/Person> . 
 } LIMIT 15 
~~~~~~~~

{linenos=off}
~~~~~~~~
Trying to get entity by name = San Francisco using SPARQL with type:
~~~~~~~~
{lang="sparql",linenos=off}
~~~~~~~~
select distinct ?s ?comment { ?s ?p "San Francisco"@en . 
 ?s <http://www.w3.org/2000/01/rdf-schema#comment> ?comment . 
   FILTER ( lang ( ?comment ) = 'en' ) . 
 ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>
    <http://dbpedia.org/ontology/City> . 
 } LIMIT 15 
~~~~~~~~
{linenos=off}
~~~~~~~~
SPARQL to get PERSON data for <http://dbpedia.org/resource/Steve_Jobs>:
~~~~~~~~
{lang="sparql",linenos=off}
~~~~~~~~
SELECT DISTINCT ?label ?comment 
 ( GROUP_CONCAT ( DISTINCT ?birthplace ; SEPARATOR=' | ' ) AS ?birthplace ) 
 ( GROUP_CONCAT ( DISTINCT ?almamater ; SEPARATOR=' | ' ) AS ?almamater ) 
 ( GROUP_CONCAT ( DISTINCT ?spouse ; SEPARATOR=' | ' ) AS ?spouse ) { 
 <http://dbpedia.org/resource/Steve_Jobs> 
   <http://www.w3.org/2000/01/rdf-schema#comment>
   ?comment . 
 FILTER ( lang ( ?comment ) = 'en' ) . 
 OPTIONAL { <http://dbpedia.org/resource/Steve_Jobs>
            <http://dbpedia.org/ontology/birthPlace>
            ?birthplace } . 
 OPTIONAL { <http://dbpedia.org/resource/Steve_Jobs> 
            <http://dbpedia.org/ontology/almaMater> 
            ?almamater } . 
 OPTIONAL { <http://dbpedia.org/resource/Steve_Jobs> 
            <http://dbpedia.org/ontology/spouse> 
            ?spouse } . 
 OPTIONAL { <http://dbpedia.org/resource/Steve_Jobs> 
            <http://www.w3.org/2000/01/rdf-schema#label> 
            ?label . 
 FILTER ( lang ( ?label ) = 'en' ) } 
 } LIMIT 10 
~~~~~~~~
{linenos=off}
~~~~~~~~
SPARQL to get CITY data for <http://dbpedia.org/resource/San_Francisco>:
~~~~~~~~
{lang="sparql",linenos=off}
~~~~~~~~
SELECT DISTINCT ?label ?comment 
 ( GROUP_CONCAT ( DISTINCT ?latitude_longitude ; SEPARATOR=' | ' ) 
     AS ?latitude_longitude ) 
 ( GROUP_CONCAT ( DISTINCT ?populationDensity ; SEPARATOR=' | ' ) 
     AS ?populationDensity ) 
 ( GROUP_CONCAT ( DISTINCT ?country ; SEPARATOR=' | ' ) 
     AS ?country ) { 
 <http://dbpedia.org/resource/San_Francisco> 
   <http://www.w3.org/2000/01/rdf-schema#comment>
   ?comment . 
      FILTER ( lang ( ?comment ) = 'en' ) . 
 OPTIONAL { <http://dbpedia.org/resource/San_Francisco> 
            <http://www.w3.org/2003/01/geo/wgs84_pos#geometry> 
            ?latitude_longitude } . 
 OPTIONAL { <http://dbpedia.org/resource/San_Francisco> 
            <http://dbpedia.org/ontology/PopulatedPlace/populationDensity> 
            ?populationDensity } . 
 OPTIONAL { <http://dbpedia.org/resource/San_Francisco> 
            <http://dbpedia.org/ontology/country> 
            ?country } . 
 OPTIONAL { <http://dbpedia.org/resource/San_Francisco> 
            <http://www.w3.org/2000/01/rdf-schema#label> 
            ?label . } 
 } LIMIT 30 
~~~~~~~~
{linenos=off}
~~~~~~~~
SPARQL to get COMPANY data for <http://dbpedia.org/resource/Apple_Inc.>:
~~~~~~~~
{lang="sparql",linenos=off}
~~~~~~~~
SELECT DISTINCT ?label ?comment ( GROUP_CONCAT ( DISTINCT ?industry ; SEPARATOR=' | ' ) 
      AS ?industry ) 
 ( GROUP_CONCAT ( DISTINCT ?netIncome ; SEPARATOR=' | ' ) 
      AS ?netIncome ) 
 ( GROUP_CONCAT ( DISTINCT ?numberOfEmployees ; SEPARATOR=' | ' ) 
      AS ?numberOfEmployees ) { 
 <http://dbpedia.org/resource/Apple_Inc.> 
    <http://www.w3.org/2000/01/rdf-schema#comment> ?comment . 
 FILTER ( lang ( ?comment ) = 'en' ) . 
 OPTIONAL { <http://dbpedia.org/resource/Apple_Inc.> 
            <http://dbpedia.org/ontology/industry> 
            ?industry } . 
 OPTIONAL { <http://dbpedia.org/resource/Apple_Inc.> 
            <http://dbpedia.org/ontology/netIncome> ?netIncome } . 
 OPTIONAL { <http://dbpedia.org/resource/Apple_Inc.> 
            <http://dbpedia.org/ontology/numberOfEmployees> ?numberOfEmployees } . 
 OPTIONAL { <http://dbpedia.org/resource/Apple_Inc.> 
            <http://www.w3.org/2000/01/rdf-schema#label> ?label . 
              FILTER ( lang ( ?label ) = 'en' ) } 
 } LIMIT 30 
~~~~~~~~

{linenos=off}
~~~~~~~~
DISCOVERED RELATIONSHIP LINKS:
~~~~~~~~

{lang="sparql",linenos=off}
~~~~~~~~
<http://dbpedia.org/resource/Steve_Jobs>    ->
    <http://dbpedia.org/ontology/birthPlace>    ->
    <http://dbpedia.org/resource/San_Francisco>
<http://dbpedia.org/resource/Steve_Jobs>    -> 
    <http://dbpedia.org/ontology/occupation>    -> 
    <http://dbpedia.org/resource/Apple_Inc.>
<http://dbpedia.org/resource/Steve_Jobs>    -> 
    <http://dbpedia.org/ontology/board>         -> 
    <http://dbpedia.org/resource/Apple_Inc.>
<http://dbpedia.org/resource/Steve_Jobs>    -> 
    <http://www.w3.org/2000/01/rdf-schema#seeAlso> -> 
    <http://dbpedia.org/resource/Apple_Inc.>
<http://dbpedia.org/resource/Apple_Inc.>    -> 
    <http://dbpedia.org/property/founders>      -> 
    <http://dbpedia.org/resource/Steve_Jobs>
~~~~~~~~

After listing the generated SPARQL for finding information for the entities in the query, KGN searches for relationships between these entities. These discovered relationships can be seen at the end of the last listing. Please note that this step makes SPARQL queries on **O(n^2)** where **n** is the number of entities. Local caching of SPARQL queries to DBPedia helps make processing several entities possible.

In addition to showing generated SPARQL and discovered relationships in the middle text pane of the application, KGN also generates formatted results that are also displayed in the bottom text pane:

{linenos=off}
~~~~~~~~

- - - ENTITY TYPE: PEOPLE - - -

LABEL: Steve Jobs

COMMENT: Steven Paul "Steve" Jobs was an American information technology 
entrepreneur and inventor. He was the co-founder, chairman, and chief 
executive officer (CEO) of Apple Inc.; CEO and majority shareholder
of Pixar Animation Studios; a member of The Walt Disney Company's 
board of directors following its acquisition of Pixar; and founder, 
chairman, and CEO of NeXT Inc. Jobs is widely recognized as a pioneer of 
the microcomputer revolution of the 1970s and 1980s, along with Apple 
co-founder Steve Wozniak. Shortly after his death, Jobs's official 
biographer, Walter Isaacson, described him as a "creative entrepreneur 
whose passion for perfection and ferocious drive revolutionized six industries:
personal computers, animated movies, music, phones

BIRTHPLACE: http://dbpedia.org/resource/San_Francisco

ALMAMATER: http://dbpedia.org/resource/Reed_College

SPOUSE: http://dbpedia.org/resource/Laurene_Powell_Jobs

- - - ENTITY TYPE: CITIES - - -

LABEL:  San Francisco

COMMENT: San Francisco, officially the City and County of San Francisco, is the
cultural, commercial, and financial center of Northern California and
the only consolidated city-county in California. San Francisco encompasses a
land area of about 46.9 square miles (121 km2) on the northern end of the 
San Francisco Peninsula, which makes it the smallest county in the state. 
It has a density of about 18,451 people per square mile (7,124 people per km2),
making it the most densely settled large city (population greater than 
200,000) in the state of California and the second-most densely populated 
major city in the United States after New York City. San Francisco is 
the fourth-most populous city in California, after Los Angeles, San Diego, and 
San Jose, and the 13th-most populous cit

LATITUDE--LONGITUDE: POINT(-122.41666412354 37.783332824707)

POPULATION-DENSITY: 7123.97092726667

COUNTRY: http://dbpedia.org/resource/United_States

- - - ENTITY TYPE: COMPANIES - - -

LABEL: Apple Inc.

COMMENT: Apple Inc. is an American multinational technology company headquartered
in Cupertino, 
California, that designs, develops, and sells consumer electronics, 
computer software, and online services. Its hardware products include the 
iPhone smartphone, the iPad tablet computer, the Mac personal computer, the 
iPod portable media player, the Apple Watch smartwatch, and the Apple TV digital 
media player. Apple's consumer software includes the macOS and iOS operating 
systems, the iTunes media player, the Safari web browser, and the iLife and 
iWork creativity and productivity suites. Its online services include the 
iTunes Store, the iOS App Store and Mac App Store, Apple Music, and iCloud.

INDUSTRY: http://dbpedia.org/resource/Computer_hardware | 
          http://dbpedia.org/resource/Computer_software | 
          http://dbpedia.org/resource/Consumer_electronics | 
          http://dbpedia.org/resource/Corporate_Venture_Capital | 
          http://dbpedia.org/resource/Digital_distribution |
          http://dbpedia.org/resource/Fabless_manufacturing

NET-INCOME: 5.3394E10

NUMBER-OF-EMPLOYEES: 115000
~~~~~~~~

Hopefully after reading through sample output and seeing the screen shot of the application, you now have a better idea what this example application does. Now we will look at project configuration and then implementation.


TBD

TBD


When the KGN application starts a sample query is randomly chosen. Queries with many entities can take a while to process, especially when you first start using this application. Every time KGN makes a web service call to DBPedia the query and response are cached in a SQLite database in **~/.kgn_local_cache.db** which can greatly speed up the program, especially in development mode when testing a set of queries. This caching also takes some load off of the public DBPedia endpoint, which is a polite thing to do.


TBD

TBD


## Review of NLP Utilities Used in Application

Here is a quick review of NLP utilities we saw earlier:


TBD: change for my Java code:

- kbnlp:make-text-object
- kbnlp::text-human-names
- kbnlp::text-place-name
- entity-uris:find-entities-in-text
- entity-uris:pp-entities

The following code snippets from the test file *****TBD place file name** show example calls to the relevant NLP functions and the generated output:

{lang="java",linenos=off}
~~~~~~~~
~~~~~~~~


## Developing Low-Level SPARQL Utilities


TBD


{lang="java",linenos=on}
~~~~~~~~
~~~~~~~~


TBD


## Implementing the Caching Layer

While developing KGN and also using it as an end user, many SPARQL queries to DBPedia contain repeated entity names so it makes sense to write a caching layer.  We use a SQLite database "~/.kgn_local_cache.db" to store queries and responses.

The caching layer is implemented in the file ** TBD: update file name: kgn/utils.lisp** and some of the relevant code is listed here:

{lang="java",linenos=on}
~~~~~~~~

~~~~~~~~
                      
This caching layer greatly speeds up my own personal use of KGN. Without caching, queries that contain many entity references simply take too long to run. The UI for the KGN application has a menu option for clearing the local cache but I almost never use this option because growing a large cache that is tailored for the types of information I search for makes the entire system much more responsive.

## Utilities to Colorize SPARQL and Generated Output

When I first had the basic functionality of KGN working, I was disappointed by how the applicaion looked as all black text on a white background. Every editor and IDE I use colorizes text in an appropriate way so I took advantage of the function **capi::write-string-with-properties** to (fairly) easily implement color hilting SPARQL queries.

When I first had the basic functionality of KGN working, I was disappointed by how the application looked as normal text. Every editor and IDE I use colorizes text in an appropriate way so I used standard ANSI terminal escape sequences to implement color hilting SPARQL queries.

The code in the following listing is in the file **colorize.hy**.

TBD


TBD: replace the follwing with Java version:

{lang="java",linenos=on}
~~~~~~~~
(require [hy.contrib.walk [let]])
(import [io [StringIO]])

;; Utilities to add ANSI terminal escape sequences to colorize text.
;; note: the following 5 functions return string values that then need to
;;       be printed.

(defn blue [s] (.format "{}{}{}" "\033[94m" s "\033[0m"))
(defn red [s] (.format "{}{}{}" "\033[91m" s "\033[0m"))
(defn green [s] (.format "{}{}{}" "\033[92m" s "\033[0m"))
(defn pink [s] (.format "{}{}{}" "\033[95m" s "\033[0m"))
(defn bold [s] (.format "{}{}{}" "\033[1m" s "\033[0m"))

(defn tokenize-keep-uris [s]
  (.split s))

(defn colorize-sparql [s]
  (let [tokens
        (tokenize-keep-uris
          (.replace (.replace (.replace s "{" " { ") "}" " } ") "." " . "))
        ret (StringIO)] ;; ret is an output stream for a string buffer
    (for [token tokens]
      (if (> (len token) 0)
          (if (= (get token 0) "?")
              (.write ret (red token))
              (if (in
                    token
                    ["where" "select" "distinct" "option" "filter"
                     "FILTER" "OPTION" "DISTINCT" "SELECT" "WHERE"])
                  (.write ret (blue token))
                  (if (= (get token 0) "<")
                      (.write ret (bold token))
                      (.write ret token)))))
      (if (not (= token "?"))
          (.write ret " ")))
    (.seek ret 0)
    (.read ret)))
~~~~~~~~

Here is an example in the test file **TBD: put in real junit file name ** and change the following to Java printout:

{lang="java",linenos=off}
~~~~~~~~
~~~~~~~~


## Text Utilities for Queries and Results

The function **TBD: update:  display-entity-results** is passed an output stream that during repl development is passed as **t** to get output in the repl and in the application will be the output stream attached to a text pane. The argument **TBD update: r-list** is a list of results where each result is a list containing a result title and a list of key/value pairs:

{lang="java",linenos=on}
~~~~~~~~
~~~~~~~~

The function **get-URIs-in-query** in lines 8-23 simply looks for URIs and saves them in a list.

In SPARQL queries, URIs are surround by angle brackets. The following code remove the brackets and embedded URIs. The function **TBD: update:  remove-uris-from-query** simply looks for URIs in an input string and removes them:

{lang="lisp",linenos=on}
~~~~~~~~
~~~~~~~~

Here is a test:

{linenos=off}
~~~~~~~~
~~~~~~~~

Given a list of URIs, the following function makes multiple SPARQL queries to DBPedia to get more information using the function **TBD: update:  get-name-and-description-for-uri** that we will look at later:

{lang="lisp",linenos=on}
~~~~~~~~

TBD: replace with Java version:

(defun handle-URIs-in-query (query)
  (let* ((uris (get-URIs-in-query query))
         (entity-names (map 'list #'get-name-and-description-for-uri uris)))
    (mapcar #'list uris (map 'list #'second entity-names))))
~~~~~~~~

The following repl show a call to **TBD: update:  handle-URIs-in-query**:



TBD: update:

{linenos=off}
~~~~~~~~
KGN 30 > (pprint (handle-URIs-in-query "<http://dbpedia.org/resource/Bill_Gates> visited <http://dbpedia.org/resource/Apple_Inc.>"))

(("<http://dbpedia.org/resource/Apple_Inc.>"
  "Apple Inc. is an American multinational technology company headquartered in Cupertino, California, that designs, develops, and sells consumer electronics, computer software, and online services. Its hardware products include the iPhone smartphone, the iPad tablet computer, the Mac personal computer, the iPod portable media player, the Apple Watch smartwatch, and the Apple TV digital media player. Apple's consumer software includes the macOS and iOS operating systems, the iTunes media player, the Safari web browser, and the iLife and iWork creativity and productivity suites. Its online services include the iTunes Store, the iOS App Store and Mac App Store, Apple Music, and iCloud.")
 ("<http://dbpedia.org/resource/Bill_Gates>"
  "William Henry \"Bill\" Gates III (born October 28, 1955) is an American business magnate, investor, author and philanthropist. In 1975, Gates and Paul Allen co-founded Microsoft, which became the world's largest PC software company. During his career at Microsoft, Gates held the positions of chairman, CEO and chief software architect, and was the largest individual shareholder until May 2014. Gates has authored and co-authored several books."))
~~~~~~~~

The function **TBD: update:  get-entity-data-helper** processes the user's query and finds entities using both the NLP utilities from earlier in this book and by using SPARQL queries to DBPedia. Something new are calls to the function **TBD: update:  updater** (lines 10-13, 17-20, and 29-31) that is defined as an optional argument. As we will see later, we will pass in a function value in the application that updates the progress bar at the bottom of the application window.

{lang="lisp",linenos=on}
~~~~~~~~
~~~~~~~~

This function presents a CAPI popup list selector to the user  so the following listed output depends on which possible entities are selected in this list. If you run the following repl example, you will see a popup window that will ask you to verify discovered entities; the user needs to check all discovered entities that are relevant to their interests.


TBD: update:


{linenos=on}
~~~~~~~~
KGN 33 > (pprint (get-entity-data-helper "Bill Gates at Microsoft"))
((:PEOPLE
  (("<http://dbpedia.org/resource/Bill_Gates>"
    "William Henry \"Bill\" Gates III (born October 28, 1955) is an American business magnate, investor, author and philanthropist. In 1975, Gates and Paul Allen co-founded Microsoft, which became the world's largest PC software company. During his career at Microsoft, Gates held the positions of chairman, CEO and chief software architect, and was the largest individual shareholder until May 2014. Gates has authored and co-authored several books.")))
 (:COMPANIES
  (("<http://dbpedia.org/resource/Microsoft>"
    "Microsoft Corporation / 02C8ma 026Akr 0259 02CCs 0252ft, -ro 028A-, - 02CCs 0254 02D0ft/ (commonly referred to as Microsoft or MS) is an American multinational technology company headquartered in Redmond, Washington, that develops, manufactures, licenses, supports and sells computer software, consumer electronics and personal computers and services. Its best known software products are the Microsoft Windows line of operating systems, Microsoft Office office suite, and Internet Explorer and Edge web browsers. Its flagship hardware products are the Xbox video game consoles and the Microsoft Surface tablet lineup. As of 2011, it was the world's largest software maker by revenue, and one of the world's most valuable companies."))))
~~~~~~~~

The popup list in the last example looks like:

TBD


In this example there were two "Bill Gates" entities, one an early American frontiersman, the other the founder of Microsoft and I chose the latter person to continue finding information about.

After identifying all of the entities that the user intended, the function **TBD: update:  entity-results->relationship-link** in the following listing is called to make additional SPARQL queries to discover possible relationships between these entities. This function is defined in the file **TBD: update:  ui-text.lisp**.


{lang="java",linenos=on}
~~~~~~~~
~~~~~~~~

In the following repl listing we create some test data of the same form as we get from calling function **TBD: update:  get-entity-data-helper** seen in a previous listing and try calling **TBD: update:  entity-results->relationship-links** with this data:


TBD: update:


{linenos=off}
~~~~~~~~
KGN 36 > (setf results '((:PEOPLE
  (("<http://dbpedia.org/resource/Bill_Gates>"
    "William Henry \"Bill\" Gates III (born October 28, 1955) is an American business magnate, investor, author and philanthropist. In 1975, Gates and Paul Allen co-founded Microsoft, which became the world's largest PC software company. During his career at Microsoft, Gates held the positions of chairman, CEO and chief software architect, and was the largest individual shareholder until May 2014. Gates has authored and co-authored several books.")))
 (:COMPANIES
  (("<http://dbpedia.org/resource/Microsoft>"
    "Microsoft Corporation / 02C8ma 026Akr 0259 02CCs 0252ft, -ro 028A-, - 02CCs 0254 02D0ft/ (commonly referred to as Microsoft or MS) is an American multinational technology company headquartered in Redmond, Washington, that develops, manufactures, licenses, supports and sells computer software, consumer electronics and personal computers and services. Its best known software products are the Microsoft Windows line of operating systems, Microsoft Office office suite, and Internet Explorer and Edge web browsers. Its flagship hardware products are the Xbox video game consoles and the Microsoft Surface tablet lineup. As of 2011, it was the world's largest software maker by revenue, and one of the world's most valuable companies.")))))
KGN 37 > (pprint (entity-results->relationship-links results))
(("<http://dbpedia.org/resource/Bill_Gates>"
  "<http://dbpedia.org/resource/Microsoft>"
  "<http://dbpedia.org/ontology/board>")
 ("<http://dbpedia.org/resource/Microsoft>"
  "<http://dbpedia.org/resource/Bill_Gates>"
  "<http://dbpedia.org/property/founders>")
 ("<http://dbpedia.org/resource/Microsoft>"
  "<http://dbpedia.org/resource/Bill_Gates>"
  "<http://dbpedia.org/ontology/keyPerson>"))
~~~~~~~~


  ~~~~~~~~


## Writing the UI

~~~~~~~~

I showed you how to run the KGN example application earlier and I suggest that you leave the application open when reading through the user interface code.


## Wrap-up

If you enjoy running and experimenting with this example and want to modify it for your own projects then I hope that I provided a sufficient road map for you to do so.

I got the idea for the KGN application because I was spending quite a bit of time manually setting up SPARQL queries for DBPedia (and other public sources like WikiData) and I wanted to experiment with partially automating this process.

TBD
