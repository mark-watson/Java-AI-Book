# Information Gathering {#information-gathering}

We saw techniques for extracting semantic information in the chapter
on Statistical Natural Language Processing and we will augment that
material in this chapter with the
use of Reuters Open Calais web services for information extraction from
text. We will then look at information discovery in relational databases,
indexing and search tools and techniques. A good alternative to storing
gathered information in a relational database would be to use an RDF
datastore - I leave that as an exercise for you.

Open Calais
-----------

The Open Calais system was developed by Clear Forest (later acquired by
Reuters). Reuters allows free use (with registration) of their named
entity extraction web service; you can make 20,000 web service calls a
day. You need to sign up and get an access key at: www.opencalais.com.
Starting in 1999, I have developed a similar named entity extraction
system (see www.knowledgebooks.com) and I sometimes use both Open Calais
and my own system together.

The example program in this section (OpenCalaisClient.java) expects the
key to be set in your environment; on my MacBook I set (here I show a
fake key – get your own):

    OPEN_CALAIS_KEY=al4345lkea48586dgfta3129aq

You will need to make sure that this value can be obtained from a
**System.getenv()** call.

The Open Calais web services support JSON, REST, and SOAP calls. I will
use the REST architectural style in this example. The Open Calais server
returns an XML RDF payload that can be directly loaded into RDF data
stores like Sesame (see [Chapter on Semantic Web](#semantic-web)). The example
class **OpenCalaisClient** depends on a trick that may break in future
versions of the Open Calais web service: an XML comment block at the top
of the returned RDF payload lists the types of entities and their
values. For example, here is a sample of the header comments with most
of the RDF payload removed for brevity:

~~~~~~~~
    <?xml version="1.0" encoding="utf-8"?>
    <string xmlns="http://clearforest.com/">
    <!--Use of the Calais Web Service is governed by the Terms
        of Service located at http://www.opencalais.com. By
        using this service or the results of the service you
        agree to these terms of service.
    -->
    <!--Relations: 
     Country: France, United States, Spain
     Person: Hillary Clinton, Doug Hattaway, Al Gore
     City: San Francisco
     ProvinceOrState: Texas
    -->
    <rdf:RDF xmlns:rdf="http://www.w3.org/1 ..."
             xmlns:c="http://s.opencalais.com/1/pred/">
     ...
    <rdf:type ...>
      ... .
    </rdf:RDF>
    </string>
~~~~~~~~

Here we will simply parse out the relations from the comment block. If
you want to use Sesame to parse the RDF payload and load it into a local
RDF repository then you can alternatively load the returned Open Calais
response by modifying the example code from the chapter
on the Semantic Web by using:

~~~~~~~~
      StringReader sr = new StringReader(result);
      RepositoryConnection connection =
                      repository.getConnection();
      connection.add(sr, "", RDFFormat.RDFXML);
~~~~~~~~

Here are a few code snippets (incomplete code: please see the Java
source file for more details) from the file OpenCalaisClient.java:

~~~~~~~~
      public Hashtable<String, List<String>>
             getPropertyNamesAndValues(String text)
               throws MalformedURLException, IOException {
        Hashtable<String, List<String>> ret =
               new Hashtable<String, List<String>>();
~~~~~~~~

You need an Open Calais license key. The following code sets up the data
for a REST style web service call and opens a connection to the server,
makes the request, and retrieves the response in the string variable __payload__.

The Java libraries for handling HTTP connections make it simple to
make a architecture style web service call and get the response as a
text string:

~~~~~~~~
        String licenseID = System.getenv("OPEN_CALAIS_KEY");
        String content = text;
        String paramsXML = "<c:params  ... </c:params>";
        StringBuilder sb =
                new StringBuilder(content.length() + 512);
        sb.append("licenseID=").append(licenseID);
        sb.append("&content=").append(content);
        sb.append("&paramsXML=").append(paramsXML);
        String payload = sb.toString();
        URLConnection connection =
          new URL("http://api.opencalais.com ...").
              openConnection();
        connection.addRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
        connection.addRequestProperty("Content-Length",
                    String.valueOf(payload.length()));
        connection.setDoOutput(true);
        OutputStream out = connection.getOutputStream();
        OutputStreamWriter writer =
                           new OutputStreamWriter(out);
        writer.write(payload);
        writer.flush();
        // get response from Open Calais server:        
        String result = new Scanner(
                   connection.getInputStream()).
                   useDelimiter("\\Z").next();
        result = result.replaceAll("&lt;", "<").
                        replaceAll("&gt;", ">");
~~~~~~~~

The text that we are parsing looks like:

~~~~~~~~
     Country: France, United States, Spain
     Person: Hillary Clinton, Doug Hattaway, Al Gore
~~~~~~~~

so the text response is parsed to extract a list of values for each
property name contained in the string variable __result__:

~~~~~~~~
        int index1 =
            result.indexOf("terms of service.-->");
        index1 = result.indexOf("<!--", index1);
        int index2 = result.indexOf("-->", index1);
        result =
            result.substring(index1 + 4, index2 - 1 + 1);
        String[] lines = result.split("\\n");
        for (String line : lines) {
          int index = line.indexOf(":");
          if (index > -1) {
            String relation = line.substring(0, index).trim();
            String[] entities =
                  line.substring(index + 1).trim().split(",");
            for (int i = 0, size = entities.length;
                 i < size; i++) {
              entities[i] = entities[i].trim();
            }
            ret.put(relation, Arrays.asList(entities));
          }
        }
        return ret;
      }
~~~~~~~~

Again, I want to point out that the above code depends on the format of
XML comments in the returned XML payload so this code may break in the
future and require modification. Here is an example use of this API:

~~~~~~~~
      String content =
        "Hillary Clinton likes to remind Texans that ...";
      Map<String, List<String>> results =
             new OpenCalaisClient().
                 getPropertyNamesAndValues(content);
      for (String key : results.keySet()) { 
          System.out.println("  " + key + ": " +
                             results.get(key));
      }
~~~~~~~~

In this example the string value assigned to the variable **content** was
about 500 words of text from a news article; the full text can be seen
in the example data files. The output of this example code is:

~~~~~~~~
      Person: [Hillary Clinton, Doug Hattaway, Al Gore]
      Relations: []
      City: [San Francisco]
      Country: [France, United States, Spain]
      ProvinceOrState: [Texas]
~~~~~~~~

There are several ways that you might want to use named entity
identification. One idea is to create a search engine that identifies
people, places, and products in search results and offers users a linked
set of documents or web pages that discuss the same people, places,
and/or products. Another idea is to load the RDF payload returned by the
Open Calais web service calls to an RDF repository and support SPARQL
queries. You may also want to modify any content management systems
(CMS) that you use to add tags for documents maintained in a CMS; using
Open Calais you are limited to the types of entities that they extract.
This limitation is one reason why I maintain and support my own  [system
for named entity and classification](http://kbsportal.com) – I like some
flexibility in the type of semantic information that I extract from text
data. I covered some of the techniques that I use in my own work in
the [Section on named entity extraction](#named-entity-extraction) in the the Chapter on
[Statistical Natural Language Processing]{#statistical-nlp} if you decide to implement
your own system to replace or augment Open Calais.

Information Discovery in Relational Databases
---------------------------------------------

We will look at some techniques for using the JDBC meta-data APIs to
explore relational database resources where you at least have read
access rights. In order to make installation of the example programs
easier we will use the Derby pure Java database that is bundled with JDK
1.6. If you are still using JDK 1.5, please download the derby.jar file
and copy it to the “lib” directory for the Java book examples:

   http://db.apache.org/derby/

There are small differences in setting up a JDBC connection to an
embedded Derby instance rather than accessing a remote server: these
differences are not important to the material in this section, it is
mostly a matter of changing a connection call.

I will use two XML data sources (data on US states and the CIA World
FactBook) for these examples, and start with the program to insert these
XML data files into the relational database:

    src-info-disc-rdbs/CreateSampleDatabase.java

and continue with a program to print out all metadata that is
implemented in the files:

    src-info-disc-rdbs/DumpMetaData.java
    src-info-disc-rdbs/DatabaseDiscovery.java

We will not implement any specific “database spidering” applications but
I will provide some basic access techniques and give you some ideas for
using database meta data in your own projects.

### Creating a Test Derby Database Using the CIA World FactBook and Data on US States

The file test\_data/XML/FactBook.xml contains data that I obtained from
the FactBook web site and converted to XML. This XML file contains data
for individual countries and a few general regions:

~~~~~~~~
    <FactBook year="2001">
      <country name="Aruba"
               location="Caribbean, island in the ..."
               background="Discovered and claimed ..."
               climate="tropical marine; little seasonal ..."
               terrain="flat; scant vegetation"
               resources="NEGL; white sandy beaches"
               hazards="lies outside the Caribbean
                        hurricane belt"
               population="70,007 (July 2001 est.)"
               government="parliamentary democracy"
               economy="Tourism is the mainstay
                        of the Aruban ..."
               inflation="4.2% (2000 est.)"
               languages="Dutch (official), Papiamento ..."
               religions="Roman Catholic 82%,
                          Protestant 8%, ..."
               capital="Oranjestad"
               unemployment="0.6% (1999 est.)"
               industries="tourism, transshipment
                           facilities, ..."
               agriculture="aloes; livestock; fish"
               exports="**2.2 billion (including oil
                        reexports) ..."
               imports="**2.5 billion (2000 est.)"
               debt="**285 million (1996)"
               aid="**26 million (1995); note -
                    the Netherlands ..."
               internet_code=".aw"
      />
      ...
    </FactBook>
~~~~~~~~

The other sample XML file USstates.xml contains information on
individual states:

~~~~~~~~
    <USstates year="2003">
      <state name="Alabama"
             abbrev="AL"
             capital="Montgomery"
             industry="Paper, lumber and wood products ..."
             agriculture="Poultry and eggs, cattle, ..."
             population="4447100">
      ...
    </USstates>
~~~~~~~~

The example class __CreateSampleDatabases__ reads both the files FactBook.xml and USsattes.xml and creates two
tables “factbook” and “states” in a test database. The implementation of
this utility class is simple: just parsing XML data and making JDBC
calls to create and populate the two tables. You can look at the Java
source file for details.

### Using the JDBC Meta Data APIs

This chapter is about processing and using data from multiple sources.
With the wealth of data stored in relational database systems, it is
important to know how to “spider” databases much as you might need to
spider data stored on specific web sites. The example class
**DumpMetaData** shows you how to discover tables, information about table
columns, and query all tables in a specific database.

The constructor of class **DumpMetaData** is called with a database URI
and prints meta data and data to standard output. This code should be
portable to database systems other than Derby by changing the driver
name.

~~~~~~~~
    class DumpMetaData {
      public DumpMetaData(String connectionUrl)
             throws SQLException, ClassNotFoundException {
        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn =
                   DriverManager.getConnection(connectionUrl);
        System.out.println("conn: " + conn);
        Statement s = conn.createStatement();
        DatabaseMetaData md = conn.getMetaData();
            
        // Discovery all table names in this database:
        List<String> tableNames = new ArrayList<String>(5);
~~~~~~~~

We will use the method **getTables()** to fetch a list of all tables in
the database. The four arguments are:

-   String catalog: can be used when database systems support catalogs.
    We will use null to act as a wildcard match.

-   String schemaPattern: can be used when database systems support
    schemas. We will use null to act as a wildcard match.

-   String tableNamePattern: a pattern to match table names; we will use
    “%” as a wildcard match.

-   String types[]: the types of table names to return. Possible values
    include TABLE, VIEW, ALIAS, SYNONYM, and SYSTEM TABLE.

The method **getTables()** returns a **ResultSet** so we iterate through
returned values just as you would in a regular SQL query using the JDBC
APIs:

~~~~~~~~
        ResultSet table_rs =
           md.getTables(null, null, "%",
                        new String[]{"TABLE"});
        while (table_rs.next()) {
          System.out.println("Table: " +
                             table_rs.getString(3));
          tableNames.add(table_rs.getString(3));
        }
            
        // Loop over all tables printing column meta data and
        // the first row:
        for (String tableName : tableNames) {
          System.out.println("\n\n** Processing table " +
                             tableName + "\n");
          String query = "SELECT * from " + tableName;
          System.out.println(query);
          ResultSet rs = s.executeQuery(query);
          ResultSetMetaData table_meta = rs.getMetaData();
          int columnCount = table_meta.getColumnCount();
          System.out.println("\nColumn meta data for table:");
          List<String> columnNames = new ArrayList<String>(10);
          columnNames.add("");
          for (int col=1; col<=columnCount; col++) {
            System.out.println("Column " + col +  " name: " +
                              table_meta.getColumnLabel(col));
            System.out.println("  column data type: " +
                           table_meta.getColumnTypeName(col));
            columnNames.add(table_meta.getColumnLabel(col));
          }
          System.out.println("\nFirst row in table:");
          if (rs.next()) {
            for (int col=1; col<=columnCount; col++) {
              System.out.println("   " + columnNames.get(col) +
                                 ": " + rs.getString(col));
            }
          }
        }
      }
    }
~~~~~~~~

Output looks like this:

~~~~~~~~
    Table: FACTBOOK
    Table: USSTATES

    ** Processing table FACTBOOK

    SELECT * from FACTBOOK

    Column meta data for table:
    Column 1 name: NAME
      column data type: VARCHAR
    Column 2 name: LOCATION
      column data type: VARCHAR
    Column 3 name: EXPORT
      column data type: BIGINT
    Column 4 name: IMPORT
      column data type: BIGINT
    Column 5 name: DEBT
      column data type: BIGINT
    Column 6 name: AID
      column data type: BIGINT
    Column 7 name: UNEMPLOYMENT_PERCENT
      column data type: INTEGER
    Column 8 name: INFLATION_PERCENT
      column data type: INTEGER

    First row in table:
       NAME: Aruba
       LOCATION: Caribbean, island in the Caribbean Sea,
                 north of Venezuela
       EXPORT: 2200000000
       IMPORT: 2500000000
       DEBT: 285000000
       AID: 26000000
       UNEMPLOYMENT_PERCENT: 0
       INFLATION_PERCENT: 4

    ** Processing table USSTATES

    SELECT * from USSTATES

    Column meta data for table:
    Column 1 name: NAME
      column data type: VARCHAR
    Column 2 name: ABBREVIATION
      column data type: CHAR
    Column 3 name: INDUSTRY
      column data type: VARCHAR
    Column 4 name: AGRICULTURE
      column data type: VARCHAR
    Column 5 name: POPULATION
      column data type: BIGINT

    First row in table:
       NAME: Alabama
       ABBREVIATION: AL
       INDUSTRY: Paper, lumber and wood products, mining,
                 rubber and plastic products, transportation
                 equipment, apparel
       AGRICULTURE: Poultry and eggs, cattle, nursery stock,
                    peanuts, cotton, vegetables, milk,
                    soybeans
       POPULATION: 4447100
~~~~~~~~

Using the JDBC meta data APIs is a simple technique but can be very
useful for both searching many tables for specific column names and for
pulling meta data and row data into local search engines. While most
relational databases provide support for free text search of text fields
in a database it is often better to export specific text columns in a
table to an external search engine.

We will spend the rest of this chapter on index and search techniques.
While we usually index web pages and local document repositories, keep
in mind that data in relational databases can also easily be indexed
either with hand written export utilities or automated techniques using
the JDBC meta-data APIs that we used in this section.

### Using the Meta Data APIs to Discern Entity Relationships

When database schemas are defined it is usually a top down approach:
entities and their relationships are modeled and then represented as
relational database tables. When automatically searching remote
databases for information we might need to discern which entities and
their relationships exist depending on table and column names.

This is likely to be a domain specific development effort. While it is
feasible and probably useful to build a “database spider” for databases
in a limited domain (for example car parts or travel destinations) to
discern entity models and their relations, it is probably not possible
without requiring huge resources to build a system that handles multiple
data domains.

The expression “dark web” refers to information on the web that is
usually not “spidered” – information that lives mostly in relational
databases and often behind query forms. While there are current efforts
by search engine companies to determine the data domains of databases
hidden behind user entry forms using surrounding text, for most
organizations this is simply too large a problem to solve. On the other
hand, using the meta data of databases that you or your organization
have read access to for “database spidering” is a more tractable
problem.

Down to the Bare Metal: In-Memory Index and Search
--------------------------------------------------

Indexing and search technology is used in a wide range of applications.
In order to get a good understanding of index and search we will design
and implement an in-memory library in this section. In Section
[section:lucene] we will take a quick look at the Lucene library and in
the [Section on Nutch](#nutch) we will look at client programs using the Nutch
indexing and search system that is based on Lucene.

We need a way to represent data to be indexed. We will use a simple
package-visible class (no getters/setters, assumed to be in the same
package as the indexing and search class):

~~~~~~~~
    class TestDocument {
      int id;
      String text;
      static int count = 0;
      TestDocument(String text) {
        this.text = text;
        id = count++;
      }
      public String toString() {
        int len = text.length();
        if (len > 25) len = 25;
        return "[Document id: " + id + ": " +
               text.substring(0,len) + "...]";
      }
    }
~~~~~~~~

We will write a class **InMemorySearch** that indexes instances of the
**TestDocument** class and supplies an API for search. The first decision
to make is how to store the index that maps search terms to documents
that contain the search terms. One simple idea would be to use a map to
maintain a set of document IDs for each search term; something like:

      Map<String, Set<Integer>> index;

This would be easy to implement but leaves much to be desired so we will
take a different approach. We would like to rank documents by relevance
but a relevance measure just based on containing all (or most) of the
search terms is weak. We will improve the index by also storing a score
of how many times a search term occurs in a document, scaled by the
number of words in a document. Since our document model does not contain
links to other documents we will not use a Google-like page ranking
algorithm that increases the relevance of search results based on the
number of incoming links to matched documents. We will use a utility
class (again, assuming same package data visibility) to hold a document
ID and a search term count. I used generics for the first version of
this class to allow alternative types for counting word use in a
document and later changed the code to hardwiring the types for ID and
word count to native integer values for runtime efficiency and to use
less memory. Here is the second version of the code:

~~~~~~~~
    class IdCount implements Comparable<IdCount> {
      int id = 0;
      int count = 0;
      public IdCount(int k, int v) {
        this.id = k;
        this.count = v;
      }
      public String toString() {
        return "[IdCount: " + id + " : " + count + "]";
      }
      @Override
      public int compareTo(IdCount o) {
        // don't use o.count - count: avoid overflows
        if (o.count == count) return 0;
        if (o.count > count) return 1;
        return -1;
      }
    }
~~~~~~~~

We can now define the data structure for our index:

~~~~~~~~
      Map<String,TreeSet<IdCount>> index =
              new Hashtable<String, TreeSet<IdCount>>();
~~~~~~~~

The following code is used to add documents to the index. I score word
counts by dividing by the maximum word size I expect for documents; in
principle it would be better to use a **Float** value but I prefer working
with and debugging code using integers – debug output is more readable.
The reason why the number of times a word appears in a document needs to
be scaled by the the size of the document is fairly obvious: if a given
word appears once in a document with 10 words and once in another
document with 1000 words, then the word is much more relevant to finding
the first document.

~~~~~~~~
      public void add(TestDocument document) {
        Map<String,Integer> wcount = 
                 new Hashtable<String,Integer>();
        StringTokenizer st =
            new StringTokenizer(document.text.toLowerCase(),
                                " .,;:!");
        int num_words = st.countTokens();
        if (num_words == 0)  return;
        while (st.hasMoreTokens()) {
          String word = st.nextToken();
          System.out.println(word);
          if (wcount.containsKey(word)) {
            wcount.put(word, wcount.get(word) + 
                       (MAX_WORDS_PER_DOCUMENT / num_words));
          } else {
            wcount.put(word, MAX_WORDS_PER_DOCUMENT
                             / num_words);
          }
        }
        for (String word : wcount.keySet()) {
          TreeSet<IdCount> ts;
          if (index.containsKey(word)) {
            ts = index.get(word);
          } else {
            ts = new TreeSet<IdCount>();
            index.put(word, ts);
          }
          ts.add(new IdCount(document.id, wcount.get(word) *
                        MAX_WORDS_PER_DOCUMENT / num_words));
        }
      }
~~~~~~~~

If a word is in the index hash table then the hash value will be a
sorted **TreeSet** of **IdCount** objects. Sort order is in decreasing size
of the scaled word count. Notice that I converted all tokenized words in
document text to lower case but I did not stem the words. For some
applications you may want to use a word stemmer as we did in Section
the [Section on tokenizing and tagging](#tokenizing-and-tagging) in the Chapter on
[Statistical Natural Language Processing]{#statistical-nlp}.
I used the temporary hash table **wcount**
to hold word counts for the document being indexed and once **wcount** was
created and filled, then looked up the **TreeSet** for each word (creating
it if it did not yet exist) and added in new **IdCount** objects to
represent the currently indexed document and the scaled number of
occurrences for the word that is the index hash table key.

For development it is good to have a method that prints out the entire
index; the following method serves this purpose:

~~~~~~~~
      public void debug() {
        System.out.println(
              "*** Debug: dump of search index:\n");
        for (String word : index.keySet()) {
          System.out.println("\n* " + word);
          TreeSet<IdCount> ts = index.get(word);
          Iterator<IdCount> iter = ts.iterator();
          while (iter.hasNext()) {
            System.out.println("   " + iter.next());
          }
        }
      }
~~~~~~~~

Here are a few lines of example code to create an index and add three
test documents:

~~~~~~~~
      InMemorySearch ims = new InMemorySearch();
      TestDocument doc1 =
        new TestDocument("This is a test for index and
                          a test for search.");
      ims.add(doc1);
      TestDocument doc2 = 
        new TestDocument("Please test the index code.");
      ims.add(doc2);
      TestDocument doc3 =
         new TestDocument("Please test the index code
                           before tomorrow.");
      ims.add(doc3);
      ims.debug();
~~~~~~~~

The method **debug** produces the following output (most is not shown for
brevity). Remember that the variable **IdCount** contains a data pair: the
document integer **ID** and a scaled integer word count in the document.
Also notice that the **TreeSet** is sorted in descending order of scaled
word count.

~~~~~~~~
    *** Debug: dump of search index:

    * code
       [IdCount: 1 : 40000]
       [IdCount: 2 : 20285]

    * please
       [IdCount: 1 : 40000]
       [IdCount: 2 : 20285]

    * index
       [IdCount: 1 : 40000]
       [IdCount: 2 : 20285]
       [IdCount: 0 : 8181]

     ...
~~~~~~~~

Given the hash table **index** it is simple to take a list of search words
and return a sorted list of matching documents. We will use a temporary
hash table **ordered\_results** that maps document IDs to the current
search result score for that document. We tokenize the string containing
search terms, and for each search word we look up (if it exists) a score
count in the temporary map **ordered\_results** (creating a new **IdCount**
object otherwise) and increment the score count. Note that the map
**ordered\_results** is ordered later by sorting the keys by the hash
table value:

~~~~~~~~
      public List<Integer> search(String search_terms,
                                  int max_terms) {
        List<Integer> ret = new ArrayList<Integer>(max_terms);
        // temporary tree set to keep ordered search results:
        final Map<Integer,Integer> ordered_results =
                           new Hashtable<Integer,Integer>(0);
        StringTokenizer st =
             new StringTokenizer(search_terms.toLowerCase(),
                                 " .,;:!");
        while (st.hasMoreTokens()) {
          String word = st.nextToken();
          Iterator<IdCount> word_counts =
                               index.get(word).iterator();
          while (word_counts.hasNext()) {
            IdCount ts = word_counts.next();
            Integer id = ts.id;
            if (ordered_results.containsKey(id)) {
              ordered_results.put(id,
                         ordered_results.get(id) + ts.count);
            } else {
              ordered_results.put(id, ts.count);
            }
          }
        }
        List<Integer> keys =
            new ArrayList<Integer>(ordered_results.keySet()); 
        Collections.sort(keys, new Comparator<Integer>() { 
            public int compare(Integer a, Integer b) { 
                return -ordered_results.get(a).
                         compareTo(ordered_results.get(b)) ;
            } 
        }); 
        int count = 0;
      result_loop:
        for (Integer id : keys) {
          if (count++ >= max_terms) break result_loop;
          ret.add(id);
        }
        return ret;
      }
~~~~~~~~

For the previous example using the three short test documents, we can
search the index, in this case for a maximum of 2 results, using:

~~~~~~~~
        List<Integer> search_results =
                           ims.search("test index", 2);
        System.out.println("result doc IDs: "+search_results);
~~~~~~~~

getting the results:

~~~~~~~~
    result doc IDs: [1, 2]
~~~~~~~~

If you want to use this “bare metal” indexing and search library, there
are a few details that still need to be implemented. You will probably
want to persist the **TestDocument** objects and this can be done simply
by tagging the class with the **Serializable** interface and writing
serialized files using the document ID as the file name. You might also
want to serialize the **InMemorySearch** class.

While I sometimes implement custom indexing and search libraries for
projects that require a lightweight and flexible approach to indexing
and search as we did in this section, I usually use either the Lucene
search library or a combination of the Hibernate Object Relational
Mapping (ORM) library with Lucene (Hibernate Search). We will look at
Lucene in Section [section:lucene].

Indexing and Search Using Embedded Lucene
-----------------------------------------

[section:lucene]

Books have been written on the Lucene indexing and search library and in
this short section we will look at a brief application example that you
can use for a quick reference for starting Lucene based projects. I
consider Lucene to be an important tool for building intelligent text
processing systems.

Lucene supports the concept of a document with one or more fields.
Fields can either be indexed or not, and optionally stored in a
disk-based index. Searchable fields can be automatically tokenized using
either one of Lucene’s built in text tokenizers or you can supply your
customized tokenizer.

When I am starting a new project using Lucene I begin by using a
template class **LuceneManager** that you can find in the file
src-index-search/LuceneManager.java. I usually clone this file and make
any quick changes for adding fields to documents, etc. We will look at a
few important code snippets in the class **LuceneManager** and you can
refer to the source code for more details. We will start by looking at
how indices are stored and managed on disk. The class constructor stores
the file path to the Lucene disk index. You can optionally use method
**createAndClearLuceneIndex** to delete an existing Lucene index (if it
exists) and creates an empty index.

~~~~~~~~
      public LuceneManager(String data_store_file_root) {
          this.data_store_file_root = data_store_file_root;
      }
     
      public void createAndClearLuceneIndex() 
                    throws CorruptIndexException,
                           LockObtainFailedException,
                           IOException {
        deleteFilePath(new File(data_store_file_root +
                                "/lucene_index"));
        File index_dir = new File(data_store_file_root +
                                  "/lucene_index");
        new IndexWriter(index_dir,
                        new StandardAnalyzer(), true).close();
      }
~~~~~~~~

If you are using an existing disk-based index that you want to reuse,
then do **not** call method **createAndClearLuceneIndex**. The last
argument to the class **IndexWriter** constructor is a flag to create a
new index, overwriting any existing indices. I use the utility method
**deleteFilePath** to make sure that all files from any previous indices
using the same top level file path are deleted. The method
**addDocumentToIndex** is used to add new documents to the index. Here we
call the constructor for the class **IndexWriter** with a value of false
for the last argument to avoid overwriting the index each time method
**addDocumentToIndex** is called.

~~~~~~~~
      public void addDocumentToIndex(
                    String document_original_uri,
                    String document_plain_text)
              throws CorruptIndexException, IOException {
        File index_dir = 
          new File(data_store_file_root + "/lucene_index");
        writer =  new IndexWriter(index_dir,
                            new StandardAnalyzer(), false);
        Document doc = new Document();
        // store URI in index; do not index
        doc.add(new Field("uri",
                          document_original_uri,
                          Field.Store.YES,
                          Field.Index.NO));
        // store text in index; index
        doc.add(new Field("text",
                          document_plain_text,
                          Field.Store.YES,
                          Field.Index.TOKENIZED));
        writer.addDocument(doc);
        writer.optimize(); // optional
        writer.close();
      }
~~~~~~~~

You can add fields as needed when you create individual Lucene
**Document** objects but you will want to add the same fields in your
application: it is not good to have different documents in an index with
different fields. There are a few things that you may want to change if
you use this class as an implementation example in your own projects. If
you are adding many documents to the index in a short time period, then
it is inefficient to open the index, add one document, and then optimize
and close the index. You might want to add a method that passes in
collections of URIs and document text strings for batch inserts. You
also may not want to store the document text in the index if you are
already storing document text somewhere else, perhaps in a database.

There are two search methods in my **LuceneManager** class: one just
returns the document URIs for search matches and the other returns both
URIs and the original document text. Both of these methods open an
instance of **IndexReader** for each query. For high search volume
operations in a multi-threaded environment, you may want to create a
pool of **IndexReader** instances and reuse them. There are several text
analyzer classes in Lucene and you should use the same analyzer class
when adding indexed text fields to the index as when you perform
queries. In the two search methods I use the same **StandardAnalyzer**
class that I used when adding documents to the index. The following
method returns a list of string URIs for matched documents:

~~~~~~~~
      public List<String>
             searchIndexForURIs(String search_query)
                  throws ParseException, IOException {
        reader = IndexReader.open(data_store_file_root + 
                                  "/lucene_index");
        List<String> ret = new ArrayList<String>();
        Searcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        QueryParser parser =
                    new QueryParser("text", analyzer);
        Query query = parser.parse(search_query);
        Hits hits = searcher.search(query);
        for (int i = 0; i < hits.length(); i++) {
          System.out.println(
            " * * searchIndexForURIs: hit: " + hits.doc(i));
          Document doc = hits.doc(i);
          String uri = doc.get("uri");
          ret.add(uri);
        }
        reader.close();
        return ret;
      }
~~~~~~~~

The Lucene class **Hits** is used for returned search matches and here we
use APIs to get the number of hits and for each hit get back an instance
of the Lucene class **Document**. Note that the field values are retrieved
by name, in this case “uri.” The other search method in my utility class
**searchIndexForURIsAndDocText** is almost the same as
**searchIndexForURIs** so I will only show the differences:

~~~~~~~~
      public List<String[]>
             searchIndexForURIsAndDocText(
                String search_query) throws Exception {
        List<String[]> ret = new ArrayList<String[]>();
        ...
        for (int i = 0; i < hits.length(); i += 1) {
          Document doc = hits.doc(i);
          System.out.println("     * *  hit: " + 
                             hits.doc(i));
          String [] pair = 
            new String[]{doc.get("uri"), doc.get("text")};
          ret.add(pair);
        }
        ...
        return ret;
      }
~~~~~~~~

Here we also return the original text from matched documents that we get
by fetching the named field “text.” The following code snippet is an
example for using the **LuceneManager** class:

~~~~~~~~
      LuceneManager lm = new LuceneManager("/tmp");
      // start fresh: create a new index:
      lm.createAndClearLuceneIndex();
      lm.addDocumentToIndex("file://tmp/test1.txt",
         "This is a test for index and a test for search.");
      lm.addDocumentToIndex("file://tmp/test2.txt",
         Please test the index code.");
      lm.addDocumentToIndex("file://tmp/test3.txt",
         "Please test the index code before tomorrow.");
      // get URIs of matching documents:
      List<String> doc_uris = 
        lm.searchIndexForURIs("test, index");
      System.out.println("Matched document URIs: "+doc_uris);
      // get URIs and document text for matching documents:
      List<String[]> doc_uris_with_text = 
        lm.searchIndexForURIsAndDocText("test, index");
      for (String[] uri_and_text : doc_uris_with_text) {
        System.out.println("Matched document URI:  " +
                           uri_and_text[0]);
        System.out.println("        document text: " +
                           uri_and_text[1]);
      }
~~~~~~~~

and here is the sample output (with debug printout from deleting the old
test disk-based index removed):

~~~~~~~~
    Matched document URIs: [file://tmp/test1.txt,
                            file://tmp/test2.txt,
                            file://tmp/test3.txt]
    Matched document URI:  file://tmp/test1.txt
            document text: This is a test for index
                           and a test for search.
    Matched document URI:  file://tmp/test2.txt
            document text: Please test the index code.
    Matched document URI:  file://tmp/test3.txt
            document text: Please test the index code
                           before tomorrow.
~~~~~~~~

I use the Lucene library frequently on customer projects and although
tailoring Lucene to specific applications is not simple, the wealth of
options for analyzing text and maintaining disk-based indices makes
Lucene a very good tool. Lucene is also very efficient and scales well
to very large indices.

In the [Section on Nutch](#nutch) we will look at the Nutch system that is
built on top of Lucene and provides a complete turnkey (but also highly
customizable) solution to implementing search in large scale projects
where it does not make sense to use Lucene in an embedded mode as we did
in this Section.


## Indexing and Search with Nutch Clients  {#nutch}

This is the last section in this book, and we have a great topic for
finishing the book: the Nutch system that is a very useful tool for
information storage and retrieval. Out of the box, it only takes about
15 minutes to set up a “vanilla” Nutch server with the default web
interface for searching documents. Nutch can be configured to index
documents on a local file system and contains utilities for processing a
wide range of document types (Microsoft Office, OpenOffice.org, PDF,
TML, etc.). You can also configure Nutch to spider remote and local
private (usually on a company LAN) web sites.

The Nutch web site http://lucene.apache.org/nutch contains binary
distributions and tutorials for quickly setting up a Nutch system and I
will not repeat all of these directions here. What I do want to show you
is how I usually use the Nutch system on customer projects: after I
configure Nutch to periodically “spider” customer specific data sources
I then use a web services client library to integrate Nutch with other
systems that need both document repository and search functionality.

Although you can tightly couple your Java applications with Nutch using
the Nutch API, I prefer to use the OpenSearch API that is an extension
of RSS 2.0 for performing search using web service calls. OpenSearch was
originally developed for Amazon’s A9.com search engine and may become
widely adopted since it is a reasonable standard. More information on
the OpenSearch standard can be found at http://www.opensearch.org but I
will cover the basics here.

### Nutch Server Fast Start Setup

For completeness, I will quickly go over the steps I use to set up
Tomcat version 6 with Nutch. For this discussion, I assume that you have
unpacked Tomcat and changed the directory name to Tomcat6\_Nutch, that
you have removed all files from the directory Tomcat6\_Nutch/webapps/,
and that you have then moved the nutch-0.9.war file (I am using Nutch
version 0.9) to the Tomcat **webapps** directory changing its name to
**ROOT.war**:

    Tomcat6_Nutch/webapps/ROOT.war

I then move the directory nutch-0.9 to:

    Tomcat6_Nutch/nutch

The file Tomcat6\_Nutch/nutch/conf/crawl-urlfilter.txt needs to be
edited to specify a combination of local and remote data sources; here I
have configured it to spider just my http://knowledgebooks.com web site
(the only changes I had to make are the two lines, one being a comment
line containing the string “knowledgebooks.com”):

~~~~~~~~
    # skip file:, ftp:, & mailto: urls
    -^(file|ftp|mailto):
    # skip image and other suffixes we can't yet parse
    -\.(gif|GIF|jpg|JPG| ...)**
    # skip URLs containing certain characters as probable
    # queries, etc.
    -[?*!@=]
    # skip URLs with slash-delimited segment that repeats
    # 3+ times, to break loops
    -.*(/.+?)/.*?\1/.*?\1/
    # accept hosts in knowledgebooks.com
    +^http://([a-z0-9]*\.)*knowledgebooks.com/
    # skip everything else
    -.
~~~~~~~~

Additional regular expression patterns can be added for more root web
sites. Nutch will not spider any site that does not match any regular
expression pattern in the configuration file. It is important that web
search spiders properly identify themselves so it is important that you
also edit the file Tomcat6\_Nutch/nutch/conf/nutch-site.xml, following
the directions in the comments to identify yourself or your company to
web sites that you spider.

~~~~~~~~
    <?xml version="1.0"?>
    <?xml-stylesheet type="text/xsl" href="configuration.xsl"?>

    <configuration>
        <property>
          <name>http.agent.name</name>
          <value>YOUR NAME Nutch spider</value>
          <description>Test spider</description>
        </property>

        <property>
          <name>http.agent.url</name>
          <value>http://YOURDOMAIN.com</value>
          <description>URL of spider server</description>
        </property>

        <property>
          <name>http.agent.email</name>
          <value>YOUR EMAIL ADDRESS</value>
          <description>markw at markwatson dot com</description>
        </property>
    </configuration>
~~~~~~~~

Then create an empty directory:

~~~~~~~~
    Tomcat6_Nutch/nutch/urls
~~~~~~~~

and create a text file (any file name is fine) with a list of starting
URLs to spider; in this case, I will just add:

~~~~~~~~
    http://knowledgebooks.com
~~~~~~~~

Then make a small test spider run to create local indices in the
subdirectory ./crawl and start the Tomcat server interactively:

~~~~~~~~
    cd nutch/
    bin/nutch crawl urls -dir crawl -depth 3 -topN 80
    ../bin/catalina.sh run
~~~~~~~~

You can run Tomcat as a background service using “start” instead of
“run” in production mode. If you rerun the spidering process, you will
need to first delete the subdirectory ./crawl or put the new index in a
different location and copy it to ./crawl when the new index is
complete. The Nutch web app running in Tomcat will expect a subdirectory
named ./crawl in the directory where you start Tomcat.

Just to test that you have Nutch up and running with a valid index,
access the following URL (specifying localhost, assuming that you are
running Tomcat on your local computer to try this):

~~~~~~~~
    http://localhost:8080
~~~~~~~~

You can then try the OpenSearch web service interface by accessing the
URL:

~~~~~~~~
    http://localhost:8080/opensearch?query=Java%20RDF
~~~~~~~~

Since I indexed my own web site that I often change, the RSS 2.0 XML
that you get back may look different than what we see in this example:

~~~~~~~~
    <?xml version="1.0" encoding="UTF-8"?>

    <rss xmlns:nutch="http://www.nutch.org/ ...>
      <channel>
        <title>Nutch: Java RDF</title>
        <description>Nutch search results for
                     query: Java RDF</description>
        <link>http://localhost:8080/search ...</link>
        <opensearch:totalResults>1</opensearch:totalResults>
        <opensearch:startIndex>0</opensearch:startIndex>
        <opensearch:itemsPerPage>10</opensearch:itemsPerPage>

        <nutch:query>Java RDF</nutch:query>
        <item>
          <title>Knowledgebooks.com: AI  ...</title>
          <description> ...  HTML snippet ... </description>
          <link>http://knowledgebooks.com/</link>
          <nutch:site>knowledgebooks.com</nutch:site>
          <nutch:cache> ... </nutch:cache>
          <nutch:explain> ... </nutch:explain>
          <nutch:segment>20080930151220</nutch:segment>
          <nutch:digest>923fb80f9f8fd66f47d70</nutch:digest>
          <nutch:tstamp>20080930221225918</nutch:tstamp>
          <nutch:boost>1.0</nutch:boost>
        </item>
      </channel>
    </rss>
~~~~~~~~

For multiple search results, there are multiple \<item\>elements in the
returned XML data. We will write web service clients to submit remote
queries and process the returned RSS 2.0 XML data in the following section.



### Using the Nutch OpenSearch Web APIs  {#nutch-apis}


A Java OpenSearch web services client is fairly easy to write: build a
REST query URL with the search phrase URL encoded, open a
**HttpURLConnection** for this query URL, read the response, and then use
an XML parser to process the returned RSS 2.0 XML payload. We will first
look at an implementation of a Nutch client and then look at some
interesting things you can do, given your own Nutch server installation
and a client. The client class **NutchClient** has three public static
APIs:

-   **search** – Returns a list of Maps, each map having values for keys
    “title,” “description,” \`\`cache\_uri," and “link.” The title is
    the web page title, the description is an HTM snippet showing search
    terms in original web page text, the cache URI is a Nutch cache of
    the original web page, and the link is the URL to the matched web
    page.

-   **searchGetCache** – Like **search** but each Map also contains a key
    \`\`cache\_content" with a value equal to the cached HTML for the
    original web page.

-   **getCacheContent** – Use this API if you first used **search** and
    later want the cached web page.

The implementation is in the file src-index-search/NutchClient.java.
Here are a few code snippets showing the public APIs:

~~~~~~~~
      static public List<Hashtable<String,String>>
         searchGetCache(String opensearch_url, String query)
                        throws IOException,
                               ParserConfigurationException,
                              SAXException {
        return search_helper(opensearch_url, query, true);
      }
      static public List<Hashtable<String,String>>
         search(String opensearch_url, String query)
                        throws IOException,
                               ParserConfigurationException,
                               SAXException {
        return search_helper(opensearch_url, query, false);
      }
      static public String
           getCacheContent(String cache_uri)
                          throws IOException {
        URL url = new URL(cache_uri);
        URLConnection uc = url.openConnection();
        return new Scanner(uc.getInputStream()).
                   useDelimiter("\\Z").next();
      }
~~~~~~~~

The implementation of the private helper method is (reformatted to fit
the page width and with comments on the code):

~~~~~~~~
      static private List<Hashtable<String,String>> 
           search_helper(String opensearch_url,
                         String query,
                         boolean return_cache) throws ... {
        List<Hashtable<String,String>> ret =
                  new ArrayList<Hashtable<String,String>>();
~~~~~~~~

We are using a REST style call so we need to URL encode the search
terms. This involves replacing space characters with “+,” etc. A search
for “Java AI” using a Nutch server on my local laptop on port 8080 would
look like:

~~~~~~~~
http://localhost:8080/opensearch?query=Java+AI
~~~~~~~~

Java code to sccess this URI would like like:

~~~~~~~~
        String url_str = opensearch_url + "?query=" +
                         URLEncoder.encode(query, "UTF-8");
        URL url = new URL(url_str);
        URLConnection uc = url.openConnection();
        BufferedInputStream bis =
             new BufferedInputStream(uc.getInputStream());
~~~~~~~~

While I usually prefer SAX XML parsers for less memory use and
efficiency, it is easier for small XML payloads just to use the
DOM-based APIs:

~~~~~~~~
        DocumentBuilder docBuilder =
             DocumentBuilderFactory.newInstance().
                                    newDocumentBuilder();
        Document doc  = docBuilder.parse(bis);
        doc.getDocumentElement().normalize();
~~~~~~~~

Here we use the DOM XML APIs to get all “item” tags and for each “item”
tag get the text for the child nodes:

~~~~~~~~
        NodeList listItems = doc.getElementsByTagName("item");
        int numItems = listItems.getLength();
        for (int i=0; i<numItems; i++) {
          Node item = listItems.item(i);
          Hashtable<String,String> new_item =
                   new Hashtable<String,String>();
          ret.add(new_item);
          NodeList item_data = item.getChildNodes();
          int num = item_data.getLength();
          for (int n=0; n<num; n++) {
            Node data = item_data.item(n);
            String name = data.getNodeName();
~~~~~~~~

Nutch returns many extra parameters encoded as items that we do not
need. Here we just keep what we need:

~~~~~~~~
            if (name.equals("title") ||
                name.equals("description") ||
                name.equals("link")) {
              new_item.put(name, data.getTextContent());
            }
            if (name.equals("nutch:cache")) {
              new_item.put("cache_uri", data.getTextContent());
            }
          }
~~~~~~~~

We may want to optionally make another web service call to get the
cached web page for this search result. Doing this approximately doubles
the time required for a search query:

~~~~~~~~
          if (return_cache &&
             new_item.get("cache_uri")!=null) {
            new_item.put("cache_content",
                 getCacheContent(new_item.get("cache_uri")));
          }
        }
        return ret;
      }
~~~~~~~~

Here is a sample use of the client class:

~~~~~~~~
      List<Hashtable<String,String>> results =
        NutchClient.search(
           "http://localhost:8080/opensearch", "Java");
      System.out.println("results: " + results);
~~~~~~~~

and the output (edited for brevity):

~~~~~~~~
    results:
    [{cache_uri=http://localhost:8080/cached.jsp?idx=0&id=0,
      link=http://knowledgebooks.com/,
      description= ... Java AI ...,
      title=Knowledgebooks.com: AI Technology for ...},
     {cache_uri=http://localhost:8080/cached.jsp?idx=0&id=1,
      link=http://knowledgebooks.com/license.txt,
      description= .. using <span class="highlight">Java ..,
      title=http://knowledgebooks.com/license.txt}]
~~~~~~~~

The average time for a Nutch client web service call on my MacBook is
130 milliseconds when I ran both Tomcat and the Nutch web services
client are on the same laptop. Average response times will only increase
slightly when the client and the server are on the same local area
network. Average response times will be longer and less predictable when
using any of the public OpenSearch servers on the Internet.

**What can you use a search client for?** Here are a few ideas based on
my own work projects:

-   Roughly determine if two words or phrases are associated with each
    other by concatenating the words or phrases and counting the number
    of search results for the combined search query.

-   Determine if a product name or ID code is spelled correctly or if a
    company carries a product by setting up a custom Nutch instance that
    only spiders the company’s web site(s). Always follow the terms and
    conditions of a web site when setting up a spider.

-   Improve search results by adding a list of project-specific synonyms
    to the search client. Expand search terms using the synonym list.

-   If you need access to public information, spider the information
    infrequently and then perform local search queries and use the local
    page caches.

For very little effort you can set up Nutch server instances that spider
specific information servers. You can often add significant value to
application programs by adding search functionality and by using Nutch
you can locally control the information.
