# Statistical Natural Language Processing {#statistical-nlp}

I have been working in the field of Natural Language Processing (NLP) since 1982. In this
chapter we will use a few of my open source NLP projects. In the next chapter I have selected
one of many fine open source projects to provide more examples of using NLP to get you started using
NLP in your own projects.

The material in this chapter is dated but useful. It is dated because deep learning networks now far surpass the capabilities of statistics and symbolic NLP. The material is useful because most experts in AI believe that deep learning only takes us so far, and in order to reach general artificial intelligence we will use some form of hybrid deep learning, symbolic AI, and probabalistic systems.

We will cover a wide variety of techniques for processing text in this
chapter. The part of speech tagger, text categorization, clustering,
spelling, and entity extraction examples are all derived from either my
open source projects or my commercial projects. I wrote the Markov model
example code for an earlier edition of this book.

Statistical Natural Language Processing (NLP) is another form of machine learning.

I am not offering you a very formal view of Statistical Natural Language
Processing in this chapter; rather, I collected Java code that I have
been using for years on various projects and simplified it to
(hopefully) make it easier for you to understand and modify for your own
use. The web site http://nlp.stanford.edu/links/statnlp.html is an
excellent resource for both papers when you need more theory and
additional software for Statistical Natural Language Processing. For
Python programmers I can recommend the statistical NLP toolkit NLTK
(nltk.sourceforge.net) that includes an online book and is licensed
using the GPL.

## Tokenizing, Stemming, and Part of Speech Tagging Text  {#tokenizing-and-tagging}

Tokenizing text is the process of splitting a string containing text
into individual tokens. Stemming is the reduction of words to
abbreviated word roots that allow for easy comparison for equality of
similar words. Tagging is identifying what part of speech each word is
in input text. Tagging is complicated by many words having different
parts of speech depending on context (examples: “*bank* the airplane,”
“the river *bank*,” etc.) You can find the code in this section in the
code ZIP file for this book in the files
src/com/knowledgebooks/nlp/fasttag/FastTag.java and
src/com/knowledgebooks/nlp/util/Tokenizer.java. The required data files
are in the directory test\_data in the files lexicon.txt (for processing
English text) and lexicon\_medpost.txt (for processing medical text).

We will also look at a public domain word stemmer that I frequently use
in this section.

Before we can process any text we need to break text into individual
tokens. Tokens can be words, numbers and punctuation symbols. The class
**Tokenizer** has two static methods, both take an input string to
tokenize and return a list of token strings. The second method has an
extra argument to specify the maximum number of tokens that you want
returned:

~~~~~~~~
        static public List<String> wordsToList(String s)
        static public List<String> wordsToList(String s,
                                               int maxR)
~~~~~~~~

The following listing shows a fragment of example code using this class
with the output:

~~~~~~~~
       String text =
         "The ball, rolling quickly, went down the hill.";
       List<String> tokens = Tokenizer.wordsToList(text);
       System.out.println(text);
       for (String token : tokens)
          System.out.print("\""+token+"\" ");
       System.out.println();
~~~~~~~~

This code fragment produces the following output:

~~~~~~~~
    The ball, rolling quickly, went down the hill.
    "The" "ball" "," "rolling" "quickly" "," "went"
    "down" "the" "hill" "." 
~~~~~~~~

For many applications, it is better to “stem” word tokens to simplify
comparison of similar words. For example “run,” “runs,” and “running”
all stem to “run.” The stemmer that we will use, which I believe to be
in the public domain, is in the file src/public\_domain/Stemmer.java.
There are two convenient APIs defined at the end of the class, one to
stem a string of multiple words and one to stem a single word token:

~~~~~~~~
        public List<String> stemString(String str)
        public String stemOneWord(String word)
~~~~~~~~

We will use both the **FastTag** and **Stemmer** classes often in the
remainder of this chapter.

The FastTag project resulted from my using the excellent tagger written
by Eric Brill while he was at the University of Pennsylvania. He used
machine learning techniques to learn transition rules for tagging text
using manually tagged text as training examples. In reading through his
doctoral thesis I noticed that there were a few transition rules that
covered most of the cases and I implemented a simple “fast tagger” in
Common Lisp, Ruby, Scheme and Java. The Java version is in the file
src/com/knowledgebooks/nlp/fasttag/FastTag.java.

The file src/com/knowledgebooks/nlp/fasttag/README.txt contains
information on where to obtain Eric Brill’s original tagging system and
also defines the tags for both his English language lexicon and the
Medpost lexicon. Table [tab:tagpos] shows the most commonly used tags
(see the README.txt file for a complete description).

~~~~~~~~
[htdp]

l | l | l

**Tag** & **Description** & **Examples**\
NN & singular noun & dog\
NNS & plural noun & dogs\
NNP & singular proper noun & California\
NNPS & plural proper noun & Watsons\
CC & conjunction & and, but, or\
CD & cardinal number & one, two\
DT & determiner & the, some\
IN & preposition & of, in, by\
JJ & adjective & large, small, green\
JJR & comparative adjective & bigger\
JJS & superlative adjective & biggest\
PP & proper pronoun & I, he, you\
RB & adverb & slowly\
RBR & comparative adverb & slowest\
RP & particle & up, off\
VB & verb & eat\
VBN & past participle verb & eaten\
VBG & gerund verb & eating\
VBZ & present verb & eats\
WP & wh\* pronoun & who, what\
WDT & wh\* determiner & which, that\

[tab:tagpos]
~~~~~~~~

Brill’s system worked by processing manually tagged text and then
creating a list of words followed by the tags found for each word. Here
are a few random lines selected from the test\_data/lexicon.txt file:

~~~~~~~~
    Arco NNP
    Arctic NNP JJ
    fair JJ NN RB
~~~~~~~~

Here “Arco” is a proper noun because it is the name of a corporation.
The word “Arctic” can be either a proper noun or an adjective; it is
used most frequently as a proper noun so the tag “NNP” is listed before
“JJ.” The word “fair” can be an adjective, singular noun, or an adverb.

The class **Tagger** reads the file lexicon either as a resource stream
(if, for example, you put lexicon.txt in the same JAR file as the
compiled **Tagger** and **Tokenizer** class files) or as a local file. Each
line in the lexicon.txt file is passed through the utility method
**parseLine** that processes an input string using the first token in the
line as a hash key and places the remaining tokens in an array that is
the hash value. So, we would process the line “fair JJ NN RB” as a hash
key of “fair” and the hash value would be the array of strings (only the
first value is currently used but I keep the other values for future
use):

When the tagger is processing a list of word tokens, it looks each token
up in the hash table and stores the first possible tag type for the
word. In our example, the word “fair” would be assigned (possibly
temporarily) the tag “JJ.” We now have a list of word tokens and an
associated list of possible tag types. We now loop through all of the
word tokens applying eight transition rules that Eric Brill’s system
learned. We will look at the first rule in some detail; **i** is the loop
variable in the range [0, number of word tokens - 1] and **word** is the
current word at index **i**:

~~~~~~~~
      //  rule 1: DT, {VBD | VBP} --> DT, NN
      if (i > 0 && ret.get(i - 1).equals("DT")) {
          if (word.equals("VBD") ||
              word.equals("VBP") ||
              word.equals("VB")) {
                         ret.set(i, "NN");
          }
      }
~~~~~~~~

In English, this rule states that if a determiner (DT) at word token
index **i-1** is followed by either a past tense verb (VBD) or a present
tense verb (VBP) then replace the tag type at index **i** with “NN.”

I list the remaining seven rules in a short syntax here and you can look
at the Java source code to see how they are implemented:

~~~~~~~~
      rule 2: convert a noun to a number (CD) if "."
              appears in the word
      rule 3: convert a noun to a past participle if
              words.get(i) ends with "ed"
      rule 4: convert any type to adverb if it ends in "ly"
      rule 5: convert a common noun (NN or NNS) to an
              adjective if it ends with "al"
      rule 6: convert a noun to a verb if the preceding
              work is "would"
      rule 7: if a word has been categorized as a common
              anoun nd it ends with "s", then set its type
              to plural common noun (NNS)
      rule 8: convert a common noun to a present participle
              verb (i.e., a gerund)
~~~~~~~~

My FastTag tagger is not quite as accurate as Brill’s original tagger so
you might want to use his system written in C but which can be executed
from Java as an external process or with a JNI interface.

In the next section we will use the tokenizer, stemmer, and tagger from
this section to develop a system for identifying named entities in text.


## Named Entity Extraction From Text  {#named-entity-extraction}


In this section we will look at identifying names of people and places
in text. This can be useful for automatically tagging news articles with
the people and place names that occur in the articles. The “secret
sauce” for identifying names and places in text is the data in the file
test\_data/propername.ser – a serialized Java data file containing hash
tables for human and place names. This data is read in the constructor
for the class **Names**; it is worthwhile looking at the code if you have
not used the Java serialization APIs before:

~~~~~~~~
      ObjectInputStream p = new ObjectInputStream(ins);
      Hashtable lastNameHash = (Hashtable) p.readObject();
      Hashtable firstNameHash = (Hashtable) p.readObject();
      Hashtable placeNameHash = (Hashtable) p.readObject();
      Hashtable prefixHash = (Hashtable) p.readObject();
~~~~~~~~

If you want to see these data values, use code like

~~~~~~~~
      while (keysE.hasMoreElements()) {
        Object key = keysE.nextElement();
        System.out.println(key + " : " +
                           placeNameHash.get(key));
      }
~~~~~~~~

to see data values like the following:

~~~~~~~~
    Mauritius : country
    Port-Vila : country_capital
    Hutchinson : us_city
    Mississippi : us_state
    Lithuania : country
~~~~~~~~

Before we look at the entity extraction code and how it works, we will
first look at an example of using the main APIs for the **Names** class.
The following example uses the methods **isPlaceName**, **isHumanName**, and
**getProperNames**:

~~~~~~~~
      System.out.println("Los Angeles: " +
                  names.isPlaceName("Los Angeles"));
      System.out.println("President Bush: " +
                  names.isHumanName("President Bush"));           
      System.out.println("President George Bush: " +
           names.isHumanName("President George Bush"));
      System.out.println("President George W. Bush: " +
           names.isHumanName("President George W. Bush"));
      ScoredList[] ret = names.getProperNames(
            "George Bush played golf. President     \
             George W. Bush went to London England, \
             and Mexico to see Mary    \
             Smith in Moscow. President Bush will   \
             return home Monday.");
      System.out.println("Human names: " +
                         ret[0].getValuesAsString());
      System.out.println("Place names: " +
                         ret[1].getValuesAsString());
~~~~~~~~

The output from running this example is:

~~~~~~~~
    Los Angeles: true
    President Bush: true
    President George Bush: true
    President George W. Bush: true
    * place name: London,
            placeNameHash.get(name): country_capital
    * place name: Mexico,
            placeNameHash.get(name): country_capital
    * place name: Moscow, 
            placeNameHash.get(name): country_capital
    Human names: George Bush:1,
                 President George W . Bush:1,
                 Mary Smith:1,
                 President Bush:1
    Place names: London:1, Mexico:1, Moscow:1
~~~~~~~~

The complete implementation that you can read through in the source file
ExtractNames.java is reasonably simple. The methods **isHumanName** and
**isPlaceName** simply look up a string in either of the human or place
name hash tables. For testing a single word this is very easy; for
example:

~~~~~~~~
      public boolean isPlaceName(String name) {
        return placeNameHash.get(name) != null;
      }
~~~~~~~~

The versions of these APIs that handle names containing multiple words
are just a little more complicated; we need to construct a string from
the words between the starting and ending indices and test to see if
this new string value is a valid key in the human names or place names
hash tables. Here is the code for finding multi-word place names:

~~~~~~~~
      public boolean isPlaceName(List<String> words,
                                 int startIndex,
                                 int numWords) {
        if ((startIndex + numWords) > words.size()) {
          return false;
        }
        if (numWords == 1) {
          return isPlaceName(words.get(startIndex));
        }
        String s = "";
        for (int i=startIndex;
             i<(startIndex + numWords); i++) {
          if (i < (startIndex + numWords - 1)) {
            s = s + words.get(startIndex) + " ";
          } else {
            s = s + words.get(startIndex);
          }
        }
        return isPlaceName(s);
      }
~~~~~~~~

This same scheme is used to test for multi-word human names. The
top-level utility method **getProperNames** is used to find human and
place names in text. The code in **getProperNames** is intentionally easy
to understand but not very efficient because of all of the temporary
test strings that need to be constructed.

## Using the WordNet Linguistic Database  {#stat-nlp-wordnet}

The home page for the WordNet project is http://wordnet.princeton.edu
and you will need to download version 3.0 and install it on your
computer to use the example programs in this section and in Chapter
[Chapter on Information Gathering](#information-gathering). As you can see on the WordNet web
site, there are several Java libraries for accessing the WordNet data
files; we will use the JAWS library written by Brett Spell as a student
project at the Southern Methodist University. I include Brett’s library
and the example programs for this section in the directory
src-jaws-wordnet in the ZIP file for this book.

### Tutorial on WordNet

The WordNet lexical database is an ongoing research project that
includes many years of effort by professional linguists. My own use
of WordNet over the last ten years has been simple, mainly using the
database to determine synonyms (called synsets in WordNet) and looking
at the possible parts of speech of words. For reference (as taken from
the Wikipedia article on WordNet), here is a small subset of the type of
relationships contained in WordNet for verbs shown by examples (taken
from the Wikipedia article):

~~~~~~~~
hypernym
:   travel (less general) is an hypernym of movement (more general)

entailment
:   to sleep is entailed by to snore because you must be asleep to snore

Here are a few of the relations supported for nouns:

hypernyms
:   canine is a hypernym of dog since every dog is of type canine

hyponyms
:   dog (less general) is a hyponym of canine (more general)

holonym
:   building is a holonym of window because a window is part of a
    building

meronym
:   window is a meronym of building because a window is part of a
    building
~~~~~~~~

Some of the related information maintained for adjectives is:

~~~~~~~~
related nouns
:   
similar to
:   
~~~~~~~~

I find the WordNet book (*WordNet: An Electronic Lexical Database
(Language, Speech, and Communication)* by Christiane Fellbaum, 1998) to
be a detailed reference for WordNet but there have been several new
releases of WordNet since the book was published. The WordNet site and
the Wikipedia article on WordNet are also good sources of information if
you decide to make WordNet part of your toolkit:

~~~~~~~~
      http://wordnet.princeton.edu/
      http://en.wikipedia.org/wiki/WordNet
~~~~~~~~

We will Brett’s open source Java WordNet utility library in the next
section to experiment with WordNet. There are also good open source
client applications for browsing the WordNet lexical database that are
linked on the WordNet web site.

### Example Use of the JAWS WordNet Library

Assuming that you have downloaded and installed WordNet on your
computer, if you look at the data files themselves you will notice that
the data is divided into index and data files for different data types.
The JAWS library (and other WordNet client libraries for many
programming languages) provides a useful view and convenient access to
the WordNet data. You will need to define a Java property for the
location of the raw WordNet data files in order to use JAWS; on my
system I set:

~~~~~~~~
    wordnet.database.dir=/Users/markw/temp/wordnet3/dict
~~~~~~~~

The example class **WordNetTest** finds the different word senses for a given word and prints this data to
standard output. We will tweak this code slightly in the next section
where we will be combining WordNet with a part of speech tagger in
another example program.

Accessing WordNet data using Brett’s library is easy, so we will spend
more time actually looking at the WordNet data itself. Here is a sample
program that shows how to use the APIs. The class constructor makes a
connection to the WordNet data files for reuse:

~~~~~~~~
    public class WordNetTest {
      public WordNetTest() {
        database =
           WordNetDatabase.getFileInstance();
      }
~~~~~~~~

Here I wrap a JAWS utility method to return lists of synsets instead of
raw Java arrays:

~~~~~~~~
      public List<Synset> getSynsets(String word) {
        return Arrays.asList(database.getSynsets(word));
      }
      public static void main(String[] args) {
~~~~~~~~

The constant **PropertyNames.DATABASE\_DIRECTORY** is equal to “wordnet.database.dir.” It is a good idea to make sure that
you have this Java property set; if the value prints as null, then
either fix the way you set Java properties, or just set it explicitly:

~~~~~~~~
      System.setProperty(PropertyNames.DATABASE_DIRECTORY,
                       "/Users/markw/temp/wordnet3/dict");
      WordNetTest tester = new WordNetTest();
      String word = "bank";
      List<Synset> synset_list = tester.getSynsets(word);
      System.out.println("\n\n** Process word: " + word);
      for (Synset synset : synset_list) {
        System.out.println("\nsynset type:       " +
               SYNSET_TYPES[synset.getType().getCode()]);
        System.out.println("       definition: " + 
                           synset.getDefinition());
        // word forms are synonyms:
        for (String wordForm : synset.getWordForms()) {
          if (!wordForm.equals(word)) {
            System.out.println("       synonym:    " +
                               wordForm);
~~~~~~~~

Antonyms are the opposites to synonyms. Notice that antonyms are
specific to individual senses for a word. This is why I have the
following code to display antonyms inside the loop over word forms for
each word sense for “bank”:

~~~~~~~~
              // antonyms mean the opposite:
              for (WordSense antonym :
                 synset.getAntonyms(wordForm)) {
                for (String opposite :
                     antonym.getSynset().getWordForms()) {
                  System.out.println(
                            "             antonym (of " +
                            wordForm+"): " + opposite);
                }
              }
            }
          }
          System.out.println("\n");
        }
      }
      private WordNetDatabase database;
      private final static String[] SYNSET_TYPES =
                              {"", "noun", "verb"};
    }
~~~~~~~~

Using this example program, we can see the word “bank” has 18 different
“senses,” 10 noun, and 8 verb senses:

~~~~~~~~
    ** Process word: bank

    synset type:       noun
           definition: sloping land (especially the slope
                       beside a body of water)
    synset type:       noun
           definition: a financial institution that accepts
                       deposits and channels the money into
                       lending activities
           synonym:    depository financial institution
           synonym:    banking concern
           synonym:    banking company
    synset type:       noun
           definition: a long ridge or pile
    synset type:       noun
           definition: an arrangement of similar objects
                       in a row or in tiers
    synset type:       noun
           definition: a supply or stock held in reserve
                       for future use (especially in
                       emergencies)
    synset type:       noun
           definition: the funds held by a gambling house
                       or the dealer in some gambling games
    synset type:       noun
           definition: a slope in the turn of a road or
                       track; the outside is higher than
                       the inside in order to reduce the
                       effects of centrifugal force
           synonym:    cant
           synonym:    camber
    synset type:       noun
           definition: a container (usually with a slot
                       in the top) for keeping money
                       at home
           synonym:    savings bank
           synonym:    coin bank
           synonym:    money box
    synset type:       noun
           definition: a building in which the business
                       of banking transacted
           synonym:    bank building
    synset type:       noun
           definition: a flight maneuver; aircraft
                       tips laterally about its
                       longitudinal axis
                       (especially in turning)
    synset type:       verb
           definition: tip laterally
    synset type:       verb
           definition: enclose with a bank
    synset type:       verb
           definition: do business with a bank or
                       keep an account at a bank
    synset type:       verb
           definition: act as the banker in a game
                       or in gambling
    synset type:       verb
           definition: be in the banking business
    synset type:       verb
           definition: put into a bank account
           synonym:    deposit
                 antonym (of deposit): withdraw
                 antonym (of deposit): draw
                 antonym (of deposit): take out
                 antonym (of deposit): draw off
    synset type:       verb
           definition: cover with ashes so to control
                       the rate of burning
    synset type:       verb
           definition: have confidence or faith in
           synonym:    trust
                 antonym (of trust): distrust
                 antonym (of trust): mistrust
                 antonym (of trust): suspect
                 antonym (of trust): distrust
                 antonym (of trust): mistrust
                 antonym (of trust): suspect
           synonym:    swear
           synonym:    rely
~~~~~~~~

WordNet provides a rich linguistic database for human linguists but
although I have been using WordNet since 1999, I do not often use it in
automated systems. I tend to use it for manual reference and sometimes
for simple tasks like augmenting a list of terms with synonyms. In the
next two sub-sections I suggest two possible projects both involving use
of synsets (synonyms). I have used both of these suggested ideas in my
own projects with some success.

### Suggested Project: Using a Part of Speech Tagger to Use the Correct WordNet Synonyms

[section:stat~n~lp~s~ynonyms]

We saw in the [Section on using WordNet](#stat-nlp-wordnet) that WordNet will give us
both synonyms and antonyms (opposite meaning) of words. The problem is
that we can only get words with similar and opposite meanings for
specific “senses” of a word. Using the example in the [Section on using WordNet](#stat-nlp-wordnet), synonyms of the word “bank” in the sense
of a verb meaning “have confidence or faith in” are:

-   trust

-   swear

-   rely

while synonyms for “bank” in the sense of a noun meaning “a financial
institution that accepts deposits and channels the money into lending
activities” are:

-   depository financial institution

-   banking concern

-   banking company

So, it does not make too much sense to try to maintain a data map of
synonyms for a given word. It does make some sense to try to use some
information about the context of a word. We can do this with some degree
of accuracy by using the part of speech tagger from Section
[section:tokenizing-and-tagging] to at least determine that a word in a
sentence is a noun or a verb, and thus limit the mapping of possible
synonyms for the word in its current context.

### Suggested Project: Using WordNet Synonyms to Improve Document Clustering

Another suggestion for a WordNet-based project is to use the Tagger to
identify the probable part of speech for each word in all text documents
that you want to cluster, and augment the documents with sysnset
(synonym) data. You can then cluster the documents similarly to how we
will calculate document similarity in the [Section on clustering text documents by content](#text-clustering).

## Automatically Assigning Tags to Text

By tagging I mean assigning zero or more categories like “politics”,
“economy”, etc. to text based on the words contained in the text. While
the code for doing this is simple there is usually much work to do to
build a word count database for different classifications.

I have been working on commercial products for automatic tagging and
semantic extraction for about ten years (see www.knowledgebooks.com if
you are interested). In this section I will show you some simple
techniques for automatically assigning tags or categories to text using
some code snippets from my own commercial product. We will use a set of
tags for which I have collected word frequency statistics. For example,
a tag of “Java” might be associated with the use of the words “Java,”
“JVM,” “Sun,” etc. You can find my pre-trained tag data in the file:

    test_data/classification_tags.xml

The Java source code for the class **AutoTagger** is in the file:

    src-statistical-nlp/
          com/knowledgebooks/nlp/AutoTagger.java

The **AutoTagger** class uses a few data structures to keep track of both the names of tags
and the word count statistics for words associated with each tag name. I
use a temporary hash table for processing the XML input data:

~~~~~~~~
      private static
        Hashtable<String, Hashtable<String, Float>>
          tagClasses;
~~~~~~~~

The names of tags used are defined in the XML tag data file: change this
file, and you alter both the tags and behavior of this utility class.
Please note that the data in this XML file is from a small set of hand-labeled (i.e., I labelled articles as being about "religion", "politics", etc.) that I use for development. I use a much larger tagged data set in my commercial product that you can experiment with at [kbsportal.com]{#http://kbsportal.com}.

Here is a snippet of data defined in the XML tag data file describing
some words (and their scores) associated with the tag
“religion\_buddhism”:

~~~~~~~~
    <tags>
      <topic name="religion_buddhism">
        <term name="buddhism" score="52" />
        <term name="buddhist" score="50" />
        <term name="mind" score="50" />
        <term name="medit" score="41" />
        <term name="buddha" score="37" />
        <term name="practic" score="31" />
        <term name="teach" score="15" />
        <term name="path" score="14" />
        <term name="mantra" score="14" />
        <term name="thought" score="14" />
        <term name="school" score="13" />
        <term name="zen" score="13" />
        <term name="mahayana" score="13" />
        <term name="suffer" score="12" />
        <term name="dharma" score="12" />
        <term name="tibetan" score="11" />
        . . .
      </topic>
      . . .
    </tags>
~~~~~~~~

Notice that the term names are stemmed words and all lower case. There
are 28 tags defined in the input XML file included in the ZIP file for
this book.

For data access, I also maintain an array of tag names and an associated
list of the word frequency hash tables for each tag name:

~~~~~~~~
      private static String[] tagClassNames;
      private static
        List<Hashtable<String, Float>> hashes =
           new ArrayList<Hashtable<String, Float>>();
~~~~~~~~

The XML data is read and these data structures are filled during static
class load time so creating multiple instances of the class **AutoTagger** has no performance penalty in either memory use or processing time.
Except for an empty default class constructor, there is only one public
API for this class, the method **getTags**:

~~~~~~~~
      public List<NameValue<String, Float>>
                     getTags(String text) {
~~~~~~~~

The utility class **NameValue** is defined in the file:

~~~~~~~~
    src-statistical-nlp/
          com/knowledgebooks/nlp/util/NameValue.java
~~~~~~~~

To determine the tags for input text, we keep a running score for each
defined tag type. I use the internal class **SFtriple** to hold triple
values of word, score, and tag index. I choose the tags with the highest
scores as the automatically assigned tags for the input text. Scores for
each tag are calculated by taking each word in the input text, stemming
it, and if the stem is in the word frequency hash table for the tag then
add the score value in the hash table to the running sum for the tag.
You can refer to the AutoTagger.java source code for details. Here is an
example use of class **AutoTagger**:

~~~~~~~~
      AutoTagger test = new AutoTagger();
      String s = "The President went to Congress to argue
                  for his tax bill before leaving on a
                  vacation to Las Vegas to see some shows
                  and gamble.";
      List<NameValue<String, Float>> results =
                                        test.getTags(s);
      for (NameValue<String, Float> result : results) {
        System.out.println(result);
      }
~~~~~~~~

The output looks like:

~~~~~~~~
    [NameValue: news_economy : 1.0]
    [NameValue: news_politics : 0.84]
~~~~~~~~


## Text Clustering  {#text-clustering}


Clustering text documents refers to associating similar text documents with each other. The text clustering system that I have written for my own projects, in
simplified form, will be used in the section. (I converted my commercial NLP product [kbsportal.com](#http://kbsportal.com) from Java to the Clojure programming language in 2012.)

The example code in this section is inherently inefficient when clustering a large number of text documents because I
perform significant semantic processing on each text document and then
compare all combinations of documents. The runtime performance is **O(N^2^)** where **N**
is the number of text documents. If you need to cluster or compare a
very large number of documents you will probably want to use a K-Mean
clustering algorithm (search for “K-Mean clustering Java” for some open
source projects).

I use a few different algorithms to rate the similarity of any two text
documents and I will combine these depending on the requirements of the
project that I am working on:

-  Calculate the intersection of common words in the two documents.

-  Calculate the intersection of common word stems in the two documents.

-  Calculate the intersection of tags assigned to the two documents.

-  Calculate the intersection of human and place names in the two documents.

In this section we will implement the second option: calculate the
intersection of word stems in two documents. Without showing the package
and import statements, it takes just a few lines of code to implement
this algorithm when we use the **Stemmer** class.

The following listing shows the implementation of class
**ComparableDocument** with comments. We start by defining constructors
for documents defined by a **File** object and a **String** object:

~~~~~~~~
    public class ComparableDocument {
        // disable default constructor calls:
        private ComparableDocument() { }
        public ComparableDocument(File document)
                     throws FileNotFoundException {
            this(new Scanner(document).
                       useDelimiter("\\Z").next());
        }
        public ComparableDocument(String text) {
            List<String> stems =
                   new Stemmer().stemString(text);
            for (String stem : stems) {
                stem_count++;
                if (stemCountMap.containsKey(stem)) {
                    Integer count = stemCountMap.get(stem);
                    stemCountMap.put(stem, 1 + count);
                } else {
                    stemCountMap.put(stem, 1);
                }
            }
        }
~~~~~~~~

In the last constructor, I simply create a count of how many times each
stem occurs in the document.

The public API allows us to get the stem count hash table, the number of
stems in the original document, and a numeric comparison value for
comparing this document with another (this is the first version – we
will add an improvement later):

~~~~~~~~
      public Map<String, Integer> getStemMap() {
        return stemCountMap;
      }
      public int getStemCount() {
        return stem_count;
      }
      public float
             compareTo(ComparableDocument otherDocument) {
        long count = 0;
        Map<String,Integer> map2 = otherDocument.getStemMap();
        Iterator iter = stemCountMap.keySet().iterator();
        while (iter.hasNext()) {
          Object key = iter.next();
          Integer count1 = stemCountMap.get(key);
          Integer count2 = map2.get(key);
          if (count1!=null && count2!=null) {
            count += count1 * count2;
          }
        }
        return (float) Math.sqrt(
                 ((float)(count*count) /
                  (double)(stem_count *
                           otherDocument.getStemCount())))
                / 2f;
      }
      private Map<String, Integer> stemCountMap =
                        new HashMap<String, Integer>();
      private int stem_count = 0;
    }
~~~~~~~~

I normalize the return value for the method **compareTo** to return a
value of 1.0 if compared documents are identical (after stemming) and
0.0 if they contain no common stems. There are four test text documents
in the test\_data directory and the following test code compares various
combinations. Note that I am careful to test the case of comparing
identical documents:

~~~~~~~~
      ComparableDocument news1 = 
         new ComparableDocument("testdata/news_1.txt");
      ComparableDocument news2 =
         new ComparableDocument("testdata/news_2.txt");
      ComparableDocument econ1 =
         new ComparableDocument("testdata/economy_1.txt");
      ComparableDocument econ2 =
         new ComparableDocument("testdata/economy_2.txt");
      System.out.println("news 1 - news1: " +
                         news1.compareTo(news1));
      System.out.println("news 1 - news2: " +
                         news1.compareTo(news2));
      System.out.println("news 2 - news2: " +
                         news2.compareTo(news2));
      System.out.println("news 1 - econ1: " +
                         news1.compareTo(econ1));
      System.out.println("econ 1 - econ1: " +
                         econ1.compareTo(econ1));
      System.out.println("news 1 - econ2: " +
                         news1.compareTo(econ2));
      System.out.println("econ 1 - econ2: " +
                         econ1.compareTo(econ2));
      System.out.println("econ 2 - econ2: " +
                         econ2.compareTo(econ2));
~~~~~~~~

The following listing shows output that indicates mediocre results; we
will soon make an improvement that makes the results better. The output
for this test code is:

~~~~~~~~
    news 1 - news1: 1.0
    news 1 - news2: 0.4457711
    news 2 - news2: 1.0
    news 1 - econ1: 0.3649214
    econ 1 - econ1: 1.0
    news 1 - econ2: 0.32748842
    econ 1 - econ2: 0.42922822
    econ 2 - econ2: 1.0
~~~~~~~~

There is not as much differentiation in comparison scores between
political news stories and economic news stories. What is up here? The
problem is that I did not remove common words (and therefore common word
stems) when creating stem counts for each document. I wrote a utility
class **NoiseWords** for identifying both common words and their stems;
you can see the implementation in the file NoiseWords.java. Removing
noise words improves the comparison results (I added a few tests since
the last printout):

~~~~~~~~
    news 1 - news1: 1.0
    news 1 - news2: 0.1681978
    news 1 - econ1: 0.04279895
    news 1 - econ2: 0.034234844
    econ 1 - econ2: 0.26178515
    news 2 - econ2: 0.106673114
    econ 1 - econ2: 0.26178515
~~~~~~~~

Much better results! The API for com.knowledgebooks.nlp.util.NoiseWords
is:

~~~~~~~~
        public static boolean checkFor(String stem)
~~~~~~~~

You can add additional noise words to the data section in the file
NoiseWords.java, depending on your application.


## Spelling Correction

Automating spelling correction is a task that you may use for many types
of projects. This includes both programs that involve users entering
text that will be automatically processed with no further interaction
with the user and for programs that keep the user “in the loop” by
offering them possible spelling choices that they can select. I have
used five different approaches in my own work for automating spelling
correction and getting spelling suggestions:

-   An old project of mine (overly complex, but with good accuracy)

-   Embedding the GNU ASpell utility

-   Use the LGPL licensed Jazzy spelling checker (a port of the GNU
    ASpell spelling system to Java)

-   Using Peter Norvig’s statistical spelling correction algorithm

-   Using Norvig’s algorithm, adding word pair statistics


We will use the last three options of these five options in
the following three sections.

### GNU ASpell Library and Jazzy    {#section-jazzyspell}

The GNU ASpell system is a hybrid system combining letter substitution
and addition (which we will implement as a short example program in
the next section), the Soundex algorithm, and dynamic
programming. I consider ASpell to be a best of breed spelling utility
and I use it fairly frequently with scripting languages like Ruby where
it is simple to “shell out” and run external programs.

You can also “shell out” external commands to new processes in Java but
there is no need to do this if we use the LGPLed Jazzy library that is
similar to ASpell and written in pure Java. For the sake of
completeness, here is a simple example of how you would use ASpell as an
external program; first, we will run ASpell on in a command shell (not
all output is shown):

~~~~~~~~
    markw** echo "ths doog" | /usr/local/bin/aspell  -a list
    @(#) International Ispell (but really Aspell 0.60.5)
    & ths 22 0: Th's, this, thus, Th, \ldots
    & doog 6 4: dog, Doug, dong, door, \ldots
~~~~~~~~

This output is easy enough to parse; here is an example in Ruby (Python,
Perl, or Java would be similar):

~~~~~~~~
    def ASpell text
      s = `echo "#{text}" | /usr/local/bin/aspell -a list`
      s = s.split("\n")
      s.shift
      results = []
      s.each {|line|
        tokens = line.split(",")
        header = tokens[0].gsub(':','').split(' ')
        tokens[0] = header[4]
        results <<
           [header[1], header[3],
            tokens.collect {|tt| tt.strip}] if header[1]
      }
      results
    end
~~~~~~~~

I include the source code to the LGPLed Jazzy library and a test class
in the directory src-spelling-Jazzy. The Jazzy library source code is in
the sub-directory com/swabunga. We will spend no time looking at the
implementation of the Jazzy library: this short section is simply meant
to get you started quickly using Jazzy.

Here is the test code from the file SpellingJazzyTester.java:

~~~~~~~~
      File dict =
         new File("test_data/dictionary/english.0");
      SpellChecker checker =
         new SpellChecker(new SpellDictionaryHashMap(dict));
      int THRESHOLD = 10; // computational cost threshold
      System.out.println(checker.getSuggestions("runnng",
                                                THRESHOLD));
      System.out.println(checker.getSuggestions("season",
                                                THRESHOLD));
      System.out.println(checker.getSuggestions(
                                 "advantagius", THRESHOLD));
~~~~~~~~

The method **getSuggestions** returns an **ArrayList** of spelling suggestions. This example code produces the following
output:

~~~~~~~~
    [running]
    [season, seasons, reason]
    [advantageous, advantages]
~~~~~~~~

The file test\_data/dictionary/english.0 contains an alphabetically
ordered list of words, one per line. You may want to add words
appropriate for the type of text that your applications use. For
example, if you were adding spelling correction to a web site for
selling sailboats then you would want to insert manufacturer and product
names to this word list in the correct alphabetical order.

The title of this book contains the word “Practical,” so I feel fine
about showing you how to use a useful Open Source package like Jazzy
without digging into its implementation or APsell’s implementation. The
next section contains the implementation of a simple algorithm and we
will study its implementation some detail.

### Peter Norvig’s Spelling Algorithm {#section-norvigspell}


Peter Norvig designed and implemented a spelling corrector in about 20
lines of Python code. I will implement his algorithm in Java in this
section and in the next section I will
extend my implementation to also use word pair statistics (i.e., use word pairs in addition to single words).

The class **SpellingSuggestions** uses static data to create an in-memory spelling dictionary. This
initialization will be done at class load time so creating instances of
this class will be inexpensive. Here is the static initialization code
with error handling removed for brevity:

~~~~~~~~
      private static Map<String, Integer> wordCounts =
              new HashMap<String, Integer>();
      static {
        // Use Peter Norvig's training file big.txt:
        // http://www.norvig.com/spell-correct.html
        FileInputStream fstream =
                   new FileInputStream("/tmp/big.txt");
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br =
            new BufferedReader(new InputStreamReader(in));
        String line;
        while ((line = br.readLine()) != null) {
          List<String> words = Tokenizer.wordsToList(line);
          for (String word : words) {
            if (wordCounts.containsKey(word)) {
               Integer count = wordCounts.get(word);
               wordCounts.put(word, count + 1);
            } else {
               wordCounts.put(word, 1);
            }
          }
        }
        in.close();
      }
~~~~~~~~

The class has two static methods that implement the algorithm. The first
method **edits** seen in the following listing is private and returns a
list of permutations for a string containing a word. Permutations are
created by removing characters, by reversing the order of two adjacent
characters, by replacing single characters with all other characters,
and by adding all possible letters to each space between characters in
the word:

~~~~~~~~
      private static List<String> edits(String word) {
        int wordL = word.length(), wordLm1 = wordL - 1;
        List<String> possible = new ArrayList<String>();
        // drop a character:
        for (int i=0; i < wordL; ++i) {
          possible.add(word.substring(0, i) +
                       word.substring(i+1));
        }
        // reverse order of 2 characters:
        for (int i=0; i < wordLm1; ++i) {
          possible.add(word.substring(0, i) +
                       word.substring(i+1, i+2) +
                       word.substring(i, i+1) +
                       word.substring(i+2));
        }
        // replace a character in each location in the word:
        for (int i=0; i < wordL; ++i) {
          for (char ch='a'; ch <= 'z'; ++ch) {
              possible.add(word.substring(0, i) + ch +
                           word.substring(i+1));
          }
        }
        // add in a character in each location in the word:
        for (int i=0; i <= wordL; ++i) {
          for (char ch='a'; ch <= 'z'; ++ch) {
            possible.add(word.substring(0, i) + ch +
                         word.substring(i));
          }
        }
        return possible;
      }
~~~~~~~~

Here is a sample test case for the method **edits** where we call it with the word “cat” and get a list of 187 permutations:

~~~~~~~~
    [at, ct, ca, act, cta, aat, bat, cat, .., fat, ..,
     cct, cdt, cet, .., caty, catz]
~~~~~~~~

The public static method **correct** has four possible return values:

-   If the word is in the spelling hash table, simply return the word.

-   Generate a permutation list of the input word using the method
    **edits**. Build a hash table **candidates** from the permutation list
    with keys being the word count in the main hashtable **wordCounts**
    with values of the words in the permutation list. If the hash table
    **candidates** is not empty then return the permutation with the best
    key (word count) value.

-   For each new word in the permutation list, call the method **edits**
    with the word, creating a new **candidates** hash table with
    permutations of permutations. If candidates is not empty then return
    the word with the highest score.

-   Return the value of the original word (no suggestions).

~~~~~~~~
      public static String correct(String word) {
        if(wordCounts.containsKey(word)) return word;
        List<String> list = edits(word);
        /**
          * Candidate hash has word counts as keys,
          * word as value:
          */
        HashMap<Integer, String> candidates =
                      new HashMap<Integer, String>();
        for (String testWord : list) {
          if(wordCounts.containsKey(testWord)) {
            candidates.put(wordCounts.get(testWord),
                           testWord);
          }
        }
        /**
         *  If candidates is not empty, then return
         *  the word with the largest key (word
         *  count) value:
         */
        if(candidates.size() > 0) {
          return candidates.get(
                 Collections.max(candidates.keySet()));
        }
        /**
         * If the edits method does not provide a
         * candidate word that matches then we will
         * call edits again with each previous
         * permutation words.
         *
         * Note: this case occurs only about 20%
         *       of the time and obviously increases
         *       the runtime of method correct.
         */
        candidates.clear();
        for (String editWords : list) {
          for (String wrd : edits(editWords)) {
            if(wordCounts.containsKey(wrd)) {
              candidates.put(wordCounts.get(wrd),wrd);
            }
          }
        }
        if (candidates.size() > 0) {
          return candidates.get(
                 Collections.max(candidates.keySet()));
        }
        return word;
      }
~~~~~~~~

Although Peter Norvig’s spelling algorithm is much simpler than the
algorithm used in ASpell it works well. I have used Norvig’s spelling
algorithm for one customer project that had a small specific vocabulary
instead of using ASpell. We will extend Norvig’s spelling algorithm in
the next section to also take advantage of word pair statistics.



### Extending the Norvig Algorithm by Using Word Pair Statistics  {#section-norvigspellpair}

It is possible to use statistics for which words commonly appear
together to improve spelling suggestions. In my experience this is only
worthwhile when applications have two traits:

-  The vocabulary for the application is specialized. For example, a social networking site for people interested in boating might want a more accurate spelling system than one that has to handle more general English text. In this example, common word pairs might be multi-word boat and manufacturer names, boating locations, etc.

-  There is a very large amount of text in this limited subject area to use for training. This is because there will be many more combinations of word pairs than words and a very large training set helps to determine which pairs are most common, rather than just coincidental.

We will proceed in a similar fashion to the implementation in the last
section but we will also keep an additional hash table containing counts
for word pairs. Since there will be many more word pair combinations
than single words, you should expect both the memory requirements and
CPU time for training to be much larger. For one project, there was so
much training data that I ended up having to use disk-based hash tables
to store word pair counts.

To make this training process take less training time and less memory to
hold the large word combination hash table, we will edit the input file
big.txt from the last section deleting the 1200 lines that contain
random words added to the end of the Project Gutenberg texts.
Furthermore, we will experiment with an even smaller version of this
file (renamed small.txt) that is about ten percent of the size of the
original training file. Because we are using a smaller training set we
should expect marginal results. For your own projects you should use as
much data as possible.

In principle, when we collect a word pair hash table where the hash
values are the number of times a word pair occurs in the training test,
we would want to be sure that we do not collect word pairs across
sentence boundaries and separate phrases occurring inside of
parenthesis, etc. For example consider the following text fragment:

    He went to Paris. The weather was warm.

Optimally, we would not want to collect statistics on word (or token)
pairs like “Paris .” or “Paris The” that include the final period in a
sentence or span a sentence. In a practical sense, since we will be
discarding seldom occurring word pairs, it does not matter too much so
in our example we will collect all tokenized word pairs at the same time
that we collect single word frequency statistics:

~~~~~~~~
      Pattern p = Pattern.compile("[,.()'\";:\\s]+");
      Scanner scanner =
              new Scanner(new File("/tmp/small.txt"));
      scanner.useDelimiter(p);
      String last = "ahjhjhdsgh";
      while (scanner.hasNext()) {
        String word = scanner.next();
        if (wordCounts.containsKey(word)) {
          Integer count = wordCounts.get(word);
          wordCounts.put(word, count + 1);
        } else {
          wordCounts.put(word, 1);
        }
        String pair = last + " " + word;
        if (wordPairCounts.containsKey(pair)) {
          Integer count = wordPairCounts.get(pair);
          wordPairCounts.put(pair, count + 1);
        } else {
          wordPairCounts.put(pair, 1);
        }
        last = word;
      }
      scanner.close();
~~~~~~~~

For the first page of text in the test file, if we print out word pairs
that occur at least two times using this code:

~~~~~~~~
      for (String pair : wordPairCounts.keySet()) {
        if (wordPairCounts.get(pair) > 1) {
          System.out.println(pair + ": " +
                             wordPairCounts.get(pair));
        }
      }
~~~~~~~~

then we get this output:

~~~~~~~~
    Arthur Conan: 3
    by Sir: 2
    of Sherlock: 2
    Project Gutenberg: 5
    how to: 2
    The Adventures: 2
    Sherlock Holmes: 2
    Sir Arthur: 3
    Adventures of: 2
    information about: 2
    Conan Doyle: 3
~~~~~~~~

The words “Conan” and “Doyle” tend to appear together frequently. If we
want to suggest spelling corrections for “the author Conan *Doyyle*
wrote” it seems intuitive that we can prefer the correction “Doyle”
since if we take the possible list of corrections for “*Doyyle*” and
combine each with the preceding word “Conan” in the text, then we notice
that the hash table **wordPairCounts** has a relatively high count for the key “Conan Doyle” that is a single
string containing a word pair.

In theory this may look like a good approach, but there are a few things
that keep this technique from being generally practical:

-   It is computationally expensive to train the system for large
    training text.

-   It is more expensive computationally to perform spelling
    suggestions.

-   The results are not likely to be much better than the single word
    approach unless the text is in one narrow domain and you have a lot
    of training text.

In the example of misspelling *Doyyle*, calling the method **edits**: **edits("Doyyle")** returns a list with 349 elements.

The method **edits** is identical to the one word spelling corrector in the last section. I
changed the method **correct** by adding an argument for the previous word, factoring in statistics
from the word pair count hash table, and for this example by not
calculating “edits of edits” as we did in the last section. Here is the
modified code:

~~~~~~~~
      public  String correct(String word,
                             String previous_word) {
        if(wordCounts.containsKey(word)) return word;
        List<String> list = edits(word);
        // candidate hash has as word counts
        // as keys, word as value:
        HashMap<Integer, String> candidates = 
                     new HashMap<Integer, String>();
        for (String testWord : list) {
          // look for word pairs with testWord in the
          // second position:
          String word_pair = previous_word + " " + testWord;
          int count_from_1_word = 0;
          int count_from_word_pairs = 0;
          if(wordCounts.containsKey(testWord)) {
            count_from_1_word += wordCounts.get(testWord);
            candidates.put(wordCounts.get(testWord),
                                          testWord);
          }
          if (wordPairCounts.containsKey(word_pair)) {
            count_from_word_pairs += 
                       wordPairCounts.get(word_pair);
          }
          // look for word pairs with testWord in the
          // first position:
          word_pair = testWord + " " + previous_word;
          if (wordPairCounts.containsKey(word_pair)) {
            count_from_word_pairs +=
                        wordPairCounts.get(word_pair);
          }
          int sum = count_from_1_word +
                    count_from_word_pairs;
          if (sum > 0)  {
            candidates.put(sum, testWord);
          }
        }
        /**
         * If candidates is not empty, then return the 
         * word with the largest key (word count) value:
         */
        if(candidates.size() > 0) {
          return candidates.get(
                  Collections.max(candidates.keySet()));
        }
        return word;
      }
~~~~~~~~

Using word pair statistics can be a good technique if you need to build
an automated spelling corrector that only needs to work on text in one
subject area. You will need a lot of training text in your subject area
and be prepared for extra work performing the training: as I mentioned
before, for one customer project I could not fit the word pair hash
table in memory (on the server that I had to use) so I had to use a
disk-based hash table – the training run took a long while. Another good
alternative for building systems for handling text in one subject area
is to augment a standard spelling library like ASpell or Jazzy with
custom word dictionaries.


## Hidden Markov Models

We previously used a set of rules to
assign parts of speech tags to words in English text. The rules that we
used were a subset of the automatically generated rules that Eric
Brill’s machine learning thesis project produced. His thesis work used
Markov modeling to calculate the most likely tag of words, given
preceding words. He then generated rules for tagging – some of which we
saw when tokenizing and tagging text where we saw Brill’s
published results of the most useful learned rules made writing a fast
tagger relatively easy.

In this section we will use word-use statistics to assign word type tags
to each word in input text. We will look in some detail at one of the
most popular approaches to tagging text: building Hidden Markov Models
(HMM) and then evaluating these models against input text to assign word
use (or part of speech) tags to words.

A complete coverage of the commonly used techniques for training and
using HMM is beyond the scope of this section. A full reference for
these training techniques is *Foundations of Statistical Natural
Language Processing* [Manning, Schutze, 1999]. We will discuss the
training algorithms and sample Java code that implements HMM. The
example in this chapter is purposely pedantic: the example code is
intended to be easy to understand and experiment with.

In Hidden Markov Models (HMM), we speak of an observable sequence of
events that moves a system through a series of states. We attempt to
assign transition probabilities based on the recent history of states of
the system (or, the last few events).

In this example, we want to develop an HMM that attempts to assign part
of speech tags to English text. To train an HMM, we will assume that we
have a large set of training data that is a sequence of words and a
parallel sequence of manually assigned part of speech tags. We will see
an example of this marked up training text that looks like “John/NNP
chased/VB the/DT dog/NN” later in this section.

For developing a sample Java program to learn how to train a HMM, we
assume that we have two Java lists words and tags that are of the same
length. So, we will have one list of words like [“John”, “chased”,
“the”, “dog”] and an associated list of part of speech tags like [“NNP”,
“VB”, “DT”, “NN”].

Once the HMM is trained, we will write another method **test\_model** that takes as input a Java vector of words and returns a Java vector of
calculated part of speech tags.

We now describe the assumptions made for Markov Models and Hidden Markov
Models using this part of speech tagging problem. First, assume that the
desired part of speech tags are an observable sequence like:

~~~~~~~~
t[1], t[2], t[3], ..., t[N]
~~~~~~~~

and the original word sequence is:

~~~~~~~~
w[1], w[2], w[3], ..., w[N]
~~~~~~~~

We will also assume that the probability of tag t[M] having a specific
value is only a function of:

~~~~~~~~
t[M-1]
~~~~~~~~

and:

~~~~~~~~
w[M] and w[M-1]
~~~~~~~~

Here we are only using the last state: in some applications, instead of
using the last observed state, we might use the last two states, greatly
increasing the resources (CPU time and memory) required for training.

For our example, we will assume that we have a finite lexicon of words.
We will use a hash table that uses the words in the lexicon as keys and
the values are the possible parts of speech.

For example, assuming the lexicon hash table is named lexicon, we use
the notation:

~~~~~~~~
lexicon["a-word"] -> list of possible tags
~~~~~~~~

The following table shows some of the possible tags used in our
example system.

|Tag Name | Part of Speech|
|---------|---------------|
|VB       | verb          |
|NN       | noun          |
|ADJ      | adjective     |
|ADV      | adverb        |
|IN       | preposition   |
|NNP      | noun          |

As an example, we might have a lexicon entry:

lexicon["bank"] -> NN, VB

where the work “bank” could be a noun (“I went to the bank”) or a verb
(“To turn, bank the airplane”). In the example program, I use a hash
table to hold the lexicon in the file Markov.java:

~~~~~~~~
      Map<String,List<String>> lexicon =
              new Hashtable<String,List<String>>();
~~~~~~~~

Another hash table keeps a count of how frequently each tag is used:

~~~~~~~~
      Map<String, Integer> tags =
              new Hashtable<String, Integer>();
      Map<String, Integer> words =
              new Hashtable<String, Integer>();
~~~~~~~~

As you will see in the next three tables
we will be operating on 2D arrays where in the first two tables the rows
and columns represent unique tag names and in the last table the columns
represent unique words and the columns represent unique tag names. We
use the following data structures to keep a list of unique tags and
words (a hash table will not work since we need an ordered sequence):

~~~~~~~~
      List<String> uniqueTags = new ArrayList<String>();
      List<String> uniqueWords = new ArrayList<String>();
~~~~~~~~

We will look at the training algorithm and implementation in the next
section. The following table shows the counts in the training data for transitioning from one part of speech to another. For example, the are 16 training examples of a NNP (proper noun) followed by a VB (verb) - fairly common. On the other hand, for example, there are no cases in the training data showing transitions like **JJ -> JJ**, **IN -> JJ**, etc. 

|      |JJ   |IN    |VB    |VBN   |TO     |NNP   |PRP    |NN     |RB    |VBG   |DT     |
|      |-----|------|------|------|-------|------|-------|-------|------|------|-------|
|JJ    |0.0  |0.0   |0.0   |0.0   |0.0    |0.0   |0.0    |1.0    |1.0   |0.0   |0.0    |
|IN    |0.0  |0.0   |0.0   |0.0   |0.0    |1.0   |0.0    |2.0    |0.0   |0.0   |4.0    |
|VB    |0.0  |3.0   |0.0   |0.0   |3.0    |3.0   |0.0    |1.0    |1.0   |0.0   |14.0   |
|VBN   |0.0  |0.0   |0.0   |0.0   |0.0    |1.0   |0.0    |0.0    |0.0   |0.0   |0.0    |
|TO    |0.0  |0.0   |2.0   |0.0   |0.0    |1.0   |0.0    |0.0    |0.0   |0.0   |2.0    |
|NNP   |0.0  |1.0   |16.0  |0.0   |0.0    |0.0   |0.0    |0.0    |0.0   |0.0   |0.0    |
|PRP   |0.0  |0.0   |2.0   |0.0   |0.0    |0.0   |0.0    |0.0    |0.0   |0.0   |0.0    |
|NN   |0.0  |3.0   |5.0   |1.0   |2.0    |0.0   |0.0    |1.0    |0.0   |0.0   |0.0    |
|RB   |0.0  |0.0   |0.0   |0.0   |0.0    |1.0   |1.0    |0.0    |0.0   |0.0   |0.0    |
|VBG  |0.0  |0.0   |0.0   |0.0   |0.0    |0.0   |0.0    |0.0    |0.0   |0.0   |1.0    |
|DT   |1.0  |0.0   |1.0   |0.0   |0.0    |0.0   |0.0    |25.0   |0.0   |0.0   |0.0    |


### Training Hidden Markov Models

We will be using code in the file Markov.java and I will show snippets
of this file with comments in this section. You can refer to the source
code for the complete implementation. There are four main methods in the
class **Markov**:

-   build\_words\_and\_tags()

-   print\_statistics()

-   train\_model

-   test\_model

In order to train a Markov model to tag parts of speech, we start by
building a two-dimensional array using the method **build\_words\_and\_tags** that uses the following 2D array to count transitions; part of this
array was seen in Figure [tab:markov~t~ransition]:

    tagToTagTransitionCount[uniqueTagCount][uniqueTagCount]

where the first index is the index of **tag~n~** and the second index is the index of **tag~n+1~**.

We will see later how to calculate the values in this array and then
how the values in this two-dimensional array will be used to calculate
the probabilities of transitioning from one tag to another. First
however, we simply use this array for counting transitions between pairs
of tags. The purpose of the training process is to fill this array with
values based on the hand-tagged training file:

training\_data/markov/tagged\_text.txt

That looks like this:

~~~~~~~~
    John/NNP chased/VB the/DT dog/NN down/RP the/DT
    street/NN ./.  I/PRP saw/VB John/NNP dog/VB
    Mary/NNP and/CC later/RB Mary/NNP throw/VB
    the/DT ball/NN to/TO John/NNP on/IN the/DT
    street/NN ./.
~~~~~~~~

The method **build\_words\_and\_tags** parses this text file and fills the **uniqueTags** and **uniqueWords** collections.

The method **train\_model** starts by filling the tag to tag transition count array(see the table
showing counts in the training data for transitioning from one part of speech to another in the last section):

    tagToTagTransitionCount[][]

The element **tagToTagTransitionCount[indexTag0][indexTag1]**
is incremented whenever we find a transition of **tag_n** to **tag_{n+1}**
in the input training text. The example program writes a spreadsheet
style CSV file for this and other two-dimensional arrays that are useful
for viewing intermediate training results in any spreadsheet program. We
normalized the data seen in Table [tab:markov~t~ransition] by dividing
each element by the count of the total number of tags. This normalized
data can be seen in the table for counts in the training data for transitioning from one part of speech to another seen in the last section. The code
for this first step is:

~~~~~~~~
      // start by filling in the tag to tag transition
      // count matrix:
      tagToTagTransitionCount =
             new float[uniqueTagCount][uniqueTagCount];
      p("tagCount="+tagCount);
      p("uniqueTagCount="+uniqueTagCount);
      for (int i = 0; i < uniqueTagCount; i++) {
        for (int j = 0; j < uniqueTagCount; j++) {
          tagToTagTransitionCount[i][j] = 0;
        }
      }
      String tag1 = (String) tagList.get(0);
      int index1 = uniqueTags.indexOf(tag1); // inefficient
      int index0;
      for (int i = 0, size1 = wordList.size() - 1;
           i < size1; i++) {
        index0 = index1;
        tag1 = (String) tagList.get(i + 1);
        index1 = uniqueTags.indexOf(tag1);   // inefficient
        tagToTagTransitionCount[index0][index1]++;
      }
      WriteCSVfile(uniqueTags, uniqueTags,
                   tagToTagTransitionCount, "tag_to_tag");
~~~~~~~~

Note that all calls to the utility method **WriteCSVfile** are for debug only: if you use this example on a large training set
(i.e., a large text corpus like Treebank of hand-tagged text) then these
2D arrays containing transition and probability values will be very
large so viewing them with a spreadsheet is convenient.

Then the method **train\_model** calculates the probabilities of transitioning from tag[N] to tag[M] (see
the table showing the raw transition counts in the last section; the following data is the same except that it is normalized to probabilities).

|      |JJ   |IN    |VB    |VBN   |TO     |NNP   |PRP    |NN     |RB    |VBG   |DT     |
|      |-----|------|------|------|-------|------|-------|-------|------|------|-------|
|JJ    |0.0  |0.0   |0.0   |0.0   |0.0    |0.0   |0.0    |0.5    |0.5   |0.0   |0.0    |
|IN    |0.0  |0.0   |0.0   |0.0   |0.0    |0.14  |0.0    |0.29   |0.0   |0.0   |0.57   |
|VB    |0.0  |0.11  |0.0   |0.0   |0.11   |0.11  |0.0    |0.04   |0.04  |0.0   |0.52   |
|VBN   |0.0  |0.0   |0.0   |0.0   |0.0    |1.0   |0.0    |0.0    |0.0   |0.0   |0.0    |
|TO    |0.0  |0.0   |0.40  |0.0   |0.0    |0.20  |0.0    |0.0    |0.0   |0.0   |0.40   |
|NNP   |0.0  |0.05  |0.76  |0.0   |0.0    |0.0   |0.0    |0.0    |0.0   |0.0   |0.0    |
|PRP   |0.0  |0.0   |0.67  |0.0   |0.0    |0.0   |0.0    |0.0    |0.0   |0.0   |0.0    |
|NN    |0.0  |3.0   |0.16  |0.03  |0.06   |0.0   |0.0    |0.03   |0.0   |0.0   |0.0    |
|RB    |0.0  |0.0   |0.0   |0.0   |0.0    |0.33  |0.33   |0.0    |0.0   |0.0   |0.0    |
|VBG   |0.0  |0.0   |0.0   |0.0   |0.0    |0.0   |0.0    |0.0    |0.0   |0.0   |1.0    |
|DT    |0.04 |0.0   |0.04  |0.0   |0.0    |0.0   |0.0    |0.93   |0.0   |0.0   |0.0    |



Here is the code for calculating these transition probabilities:

~~~~~~~~
      // now calculate the probabilities of transitioning
      // from tag[N] to tag[M]:
      probabilityTag1ToTag2 =
           new float[uniqueTagCount][uniqueTagCount];
      for (int i = 0; i < uniqueTagCount; i++) {
        int count =
         ((Integer)tags.get(
                   (String)uniqueTags.get(i))).intValue();
        p("tag: " + uniqueTags.get(i) + ", count="+count);
        for (int j = 0; j < uniqueTagCount; j++) {
          probabilityTag1ToTag2[i][j] =
            0.0001f + tagToTagTransitionCount[i][j]
                      / (float)count;
        }
      }
      WriteCSVfile(uniqueTags, uniqueTags,
                   probabilityTag1ToTag2,
                   "test_data/markov/prob_tag_to_tag");
~~~~~~~~

Finally, in the method **train\_model** we complete the training by defining the array

  probabilityWordGivenTag[uniqueWordCount][uniqueTagCount]

which shows the probability of a tag at index **N** producing a word at
index **N** in the input training text as output by the example program as shown here:

~~~~~~~~
            *JJ*   *IN*   *VB*   *VBN*   *TO*   *NNP*   *PRP*   *NN*   *RB*
  --------- ------ ------ ------ ------- ------ ------- ------- ------ ----
  went      0.00   0.00   0.07   0.00    0.00   0.00    0.00    0.00   0.00
  mary      0.00   0.00   0.00   0.00    0.00   0.52    0.00    0.00   0.00
  played    0.00   0.00   0.07   0.00    0.00   0.00    0.00    0.00   0.00
  river     0.00   0.00   0.00   0.00    0.00   0.00    0.00    0.03   0.00
  leave     0.00   0.00   0.07   0.00    0.00   0.00    0.00    0.03   0.00
  dog       0.00   0.00   0.04   0.00    0.00   0.00    0.00    0.23   0.00
  away      0.00   0.00   0.00   0.00    0.00   0.00    0.00    0.00   0.33
  chased    0.00   0.00   0.11   1.00    0.00   0.00    0.00    0.00   0.00
  at        0.00   0.14   0.00   0.00    0.00   0.00    0.00    0.00   0.00
  tired     0.50   0.00   0.04   0.00    0.00   0.00    0.00    0.00   0.00
  good      0.50   0.00   0.00   0.00    0.00   0.00    0.00    0.00   0.00
  had       0.00   0.00   0.04   0.00    0.00   0.00    0.00    0.00   0.00
  throw     0.00   0.00   0.07   0.00    0.00   0.00    0.00    0.03   0.00
  from      0.00   0.14   0.00   0.00    0.00   0.00    0.00    0.00   0.00
  so        0.00   0.00   0.00   0.00    0.00   0.00    0.00    0.00   0.33
  stayed    0.00   0.00   0.04   0.00    0.00   0.00    0.00    0.00   0.00
  absense   0.00   0.00   0.00   0.00    0.00   0.00    0.00    0.03   0.00
  street    0.00   0.00   0.00   0.00    0.00   0.00    0.00    0.06   0.00
  john      0.00   0.00   0.00   0.00    0.00   0.48    0.00    0.00   0.00
  ball      0.00   0.00   0.04   0.00    0.00   0.00    0.00    0.06   0.00
  on        0.00   0.29   0.00   0.00    0.00   0.00    0.00    0.00   0.00
  cat       0.00   0.00   0.00   0.00    0.00   0.00    0.00    0.32   0.00
  later     0.00   0.00   0.00   0.00    0.00   0.00    0.00    0.00   0.33
  she       0.00   0.00   0.00   0.00    0.00   0.00    0.33    0.00   0.00
  of        0.00   0.14   0.00   0.00    0.00   0.00    0.00    0.00   0.00
  with      0.00   0.29   0.00   0.00    0.00   0.00    0.00    0.00   0.00
  saw       0.00   0.00   0.19   0.00    0.00   0.00    0.00    0.03   0.00
~~~~~~~~

Here is the code for this last training step:

~~~~~~~~
      // now calculate the probability of a word, given
      // a proceeding tag:
      probabilityWordGivenTag =
            new float[uniqueWordCount][uniqueTagCount];
      for (int i = 0; i < uniqueWordCount; i++) {
        String tag = uniqueTags.get(j);
        for (int j = 0; j < uniqueTagCount; j++) {
          String tag = uniqueTags.get(j);
          // note: index of tag is one less than index
          // of emitted word we are testing:
          int countTagOccurence = tags.get(tag);
          float wordWithTagOccurence = 0;
          for (int n=0, sizem1=wordList.size()-1;
               n<sizem1; n++) {
            String testWord = wordList.get(n);
            String testTag  = tagList.get(n);
            if (testWord.equals(word) && 
                testTag.equals(tag)) {
              wordWithTagOccurence++;
            }
          }
          probabilityWordGivenTag[i][j] =
            wordWithTagOccurence / (float)countTagOccurence;
        }
      }
      WriteCSVfile(uniqueWords, uniqueTags,
                   probabilityWordGivenTag,
                   "test_data/markov/prob_word_given_tag");
~~~~~~~~

### Using the Trained Markov Model to Tag Text

From Section [section:markov~t~raining] we have the probabilities of a
given tag being assigned to words in the lexicon and we have the
probability of a given tag, given the preceding tag. We will use this
information in a “brute force” way in the method **test\_model** : we will iterate through all possible tagging possibilities and rate
them using the formula from *Foundations of Statistical Natural Language
Processing* [Manning/Schutze, 1999] page 347:

  **Rating = Product of terms: (P(word-i |tag-i) * P(tag-i |tag-(i-1)))**

**(P(word-i |tag-i)** is the probability of **word** having a tag value **tag** and **P(tag-i |tag-(i-1))** is the probability of **tag-i** following **tag-(i-1)**. We can simply implement two nested loops over all possible tags for
each input word and use the tag for each word with the highest rating
(score).

The arrays for these probabilities in Markov.java are **probabilityWordGivenTag** and
**probabilityTag1ToTag2**. The logic for scoring a specific tagging possibility for a sequence of
words in the method score.

The method **exponential\_tagging\_algorithm** is the top level API for tagging words. Please note that the word
sequence that you pass to **exponential\_tagging\_algorithm** must not contain any words that were not in the original training data
(i.e., in the file tagged\_text.txt).

~~~~~~~~
      public List<String>
         exponential_tagging_algorithm(List<String> words) {
        possibleTags = new ArrayList<ArrayList<String>>();
        int num = words.size();
        indices = new int[num];
        counts = new int[num];
        int [] best_indices = new int[num];
        for (int i=0; i<num; i++) {
          indices[i] = 0; counts[i] = 0;
        }
        for (int i=0; i<num; i++) {
          String word = "" + words.get(i);
          List<String> v = lexicon.get(word);
           // possible tags at index i:
          ArrayList<String> v2 = new ArrayList<String>(); 
          for (int j=0; j<v.size(); j++) {
            String tag = "" + v.get(j);
            if (v2.contains(tag) == false) {
              v2.add(tag);  counts[i]++;
            }
          }
          // possible tags at index i:
          possibleTags.add(v2);      
          System.out.print("^^ word: " + word + ",
                           tag count: " + counts[i] +
                           ", tags: ");
          for (int j=0; j<v2.size(); j++) {
            System.out.print(" " + v2.get(j));
          }
          System.out.println();
        }
        float best_score = -9999;
        do {
          System.out.print("Current indices:");
          for (int k=0; k<num; k++) {
            System.out.print(" " + indices[k]);
          }
          System.out.println();
          float score = score(words);
          if (score > best_score) {
            best_score = score;
            System.out.println(" * new best score: " + 
                               best_score);
            for (int m=0; m<num; m++) {
              best_indices[m] = indices[m];
            }
          }
        } while (incrementIndices(num)); // see text below

        List<String> tags = new ArrayList<String>(num);
        for (int i=0; i<num; i++) {
          List<String> v = possibleTags.get(i);
          tags.add(v.get(best_indices[i]));
        }
        return tags;
      }
~~~~~~~~

The method **incrementIndices** is responsible for generating the next possible tagging for a sequence
of words. Each word in a sequence can have one or more possible tags.
The method **incrementIndices** counts with a variable base per digit
position. For example, if we had four words in an input sequence with
the first and last words only having one possible tag value and the
second having two possible tag values and the third word having three
possible tag values, then **incrementIndices** would count like this:

~~~~~~~~
    0 0 0 0
    0 1 0 0
    0 0 1 0
    0 1 1 0
    0 0 2 0
    0 1 1 0
~~~~~~~~

The generated indices (i.e., each row in this listing) are stored in the
class instance variable **indices** which is used in method **score:

~~~~~~~~
      /**
       *  Increment the class variable indices[] to point
       *  to the next possible set of tags to check.
       */
      private boolean incrementIndices(int num) {
       for (int i=0; i<num; i++) {
         if (indices[i] < (counts[i] - 1)) {
            indices[i] += 1;
            for (int j=0; j<i; j++) {
              indices[j] = 0;
            }
            return true;
          }
        }
        return false;
      }                                                
~~~~~~~~

We are not using an efficient algorithm if the word sequence is long. In
practice this is not a real problem because you can break up long texts
into smaller pieces for tagging; for example, you might want to tag just
one sentence at a time. Production systems use the [Viterbi algorithm](#http://en.wikipedia.org/wiki/Viterbi_algorithm).


## Wrapup

If you have not used NLP technologies before I hope that this chapter provided a good introduction
and sufficient Java code examples to get you experimenting on your own. As I mentioned earlier
if you also program in Python you might consider installing and experimenting the
NLTK NLP framework.

If you are a Haskell programmer you might enjoy my Haskell NLP tools
that I released with an open source license in the sprint of 2014.

TBD: web url for the githug repo for my Haskell code

