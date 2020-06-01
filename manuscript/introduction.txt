Introduction
============

This is not the latest edition of this book. The fifth edition is available on [leanpub.com/javaai](https://leanpub.com/javaai).

There are many fine books on Artificial Intelligence (AI) and good
tutorials and software on the web. This book is intended for programmers
who either already have an interest in AI or need to use specific AI
technologies at work.

The material is not intended as a complete reference for AI theory.
Instead, I provide enough theoretical background to understand the
example programs and to provide a launching point if you want or need to
delve deeper into any of the topics covered. I believe that we all learn
best when we are having fun and I have tried to make the example
programs easy to run and experiment with - so enjoy yourself!

In updating this book to the fourth edition I had hoped to add in addition to the
new chapters (on the Semantic Web, Information Gathering, and Data Science) more
material on machine learning and deep learning. I would like to recommend two free
online courses that cover these topics very well: Andrew Ng's Coursera Machine Learning
class and also Geoffrey Hinton’s Coursera Machine Learning Using Neural Networks
class.

I wrote this book to provide a quick start for
Java programmers and I hope that the material in this book will
encourage you to dig deeper with available online courses.


Other JVM Languages
-------------------

The Java language and JVM platform are very widely used so that
techniques that you learn can be broadly useful. There are other JVM
languages like JRuby, Clojure, Jython, and Scala that can use existing
Java classes. While the examples in this book are written in Java you
should have little trouble using my Java example classes and the open
source libraries with these alternative JVM languages.

I do provide "wrappers" written in Clojure and JRuby for a few of the
example programs in this book that are included in the [the github repository for this book](https://github.com/mark-watson/Java-AI-Book-Code). Using the Java examples in JRuby and Clojure is covered in
Appendices A and B.


Github Repository for Book Software
-----------------------------------

The code for the example programs is available on github:

  [https://github.com/mark-watson/Java-AI-Book-Code](https://github.com/mark-watson/Java-AI-Book-Code)

All the example code that I have written is copyright Mark Watson and can be used under either of the LGPL 3 or Apache 2 licenses. Use either license, whichever works best for you.

The code examples usually consist of reusable (non GUI) libraries and
throwaway text-based test programs to solve a specific application
problem; in some cases, the test code will contain a test or
demonstration GUI.


Use of Java Generics and Native Types
-------------------------------------

In general I usually use Java generics and the new collection classes
for almost all of my Java programming. That is also the case for the
examples in this book except when using native types and arrays provides
a real performance advantage (for example, in the search examples).

Since arrays must contain reifiable types they play poorly with generics
so I prefer not to mix coding styles in the same code base. There are
some obvious cases where not using primitive types leads to excessive
object creation and boxing/unboxing. That said, I expect Java compilers,
Hotspot, and the JVM in general to keep getting better and this may be a
non-issue in the future.

Notes on Java Coding Styles Used in this Book
---------------------------------------------

Many of the example programs do not strictly follow common Java
programming idioms – this is usually done for brevity. For example, when
a short example is all in one Java package I will save lines of code and
programing listing space by not declaring class data private with public
getters and setters; instead, I will sometimes simply use package
visibility as in this example:

~~~~~~~~
  public static class Problem {
    // constants for appliance types:
    //    enum Appliance {REFRIGERATOR, MICROWAVE, TV, DVD};
    // constants for problem types:
    //    enum ProblemType {NOT_RUNNING, SMOKING, ON_FIRE,
	//                      MAKES_NOISE};
    // constants for environmental data:
    //    enum EnvironmentalDescription {CIRCUIT_BREAKER_OFF,
	//                                   LIGHTS_OFF_IN_ROOM};    
    Appliance applianceType;
    List<ProblemType> problemTypes = new ArrayList<ProblemType>();
    List<EnvironmentalDescription> environmentalData =
		         new ArrayList<EnvironmentalDescription>();
    // etc.
  } 
~~~~~~~~

Please understand that I do not advocate this style of programming in
large projects but one challenge in writing about software development
is the requirement to make the examples short and easily read and
understood. Many of the examples started as large code bases for my own
projects that I “whittled down” to a small size to show one or two
specific techniques. Forgoing the use of “getters and setters” in many
of the examples is just another way to shorten the examples.

Authors of programming books are faced with a problem in formatting
program snippets: limited page width. You will frequently see what would
be a single line in a Java source file split over two or three lines to
accommodate limited page width as seen in this example:

~~~~~~~~
     private static void createTestFacts(WorkingMemory workingMemory)
             throws Exception { ...  } 
~~~~~~~~

Book Summary
------------


[Chapter on Search](#search) deals with heuristic search in two domains:
two-dimensional grids (for example mazes) and graphs (defined by nodes
and edges connecting nodes).

[Chapter on Reasoning](#reasoning) covers logic, knowledge representation, and
reasoning using the PowerLoom system.

[Chapter on Semantic Web](#semantic-web) covers the Semantic Web. You will learn
how to use RDF and RDFS data for knowledge representation and how to use
the popular Sesame open source Semantic Web system.

[Chapter on Expert Systems](#expertsystems) introduces you to rule-based or
production systems. We will use the open source Drools system to
implement simple expert systems for solving “blocks world” problems and
to simulate a help desk system.

[Chapter on Genetic Algorithms](#ga) gives an overview of Genetic Algorithms, provides a
Java library, and solves a test problem. The chapter ends with
suggestions for projects you might want to try.

[Chapter on Neural Networks](#neural-networks) introduces Hopfield and Back
Propagation Neural Networks. In addition to Java libraries you can use
in your own projects, we will use two Swing-based Java applications to
visualize how neural networks are trained.

[Chapter on Machine Learning with Weka](#ml-weka) introduces you to the GPLed Weka project.
Weka is a best of breed toolkit for solving a wide range of machine
learning problems.

[Chapter on Statistical Natural Language Processing](#statistical-nlp) covers several
Statistical NLP techniques that I often use in my own work:
processing text (tokenizing, stemming, and determining part of speech),
named entity extraction from text, using the WordNet lexical database,
automatically assigning tags to text, text clustering, three different
approaches to spelling correction, and a short tutorial on Markov
Models.

[Chapter on Information Gathering](#information-gathering) provides useful techniques for
gathering and using information: using the Open Calais web services for
extracting semantic information from text, information discovery in
relational databases, and three different approaches to indexing and
searching text.


[Chapter on Data Science](#data-science-chapter) discusses setting up your own Java environment for Data Science tasks.

