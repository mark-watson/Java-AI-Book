Preface
=======

The latest edition of this book is always available at [https://leanpub.com/javaai](https://leanpub.com/javaai). Currently the latest edition was released in the summer of 2020. It has been 5+ years since the last edition and this is largely a rewrite, dropping some material like Drools based expert systems, and placing a heavier emphasis on machine learning, particularly neural networks and deep learning. I have also greatly expanded discussion of the semantic web and linked data including examples to generate knowledge graphs automatically from text documents and also a system to help navigate public Knowledge Graphs like DBPedia and WikiData.

I have been developing commercial Artificial Intelligence (AI) tools and applications since the 1980s.

![Mark Watson](images/Mark.png "Mark Watson")

I wrote this book for both professional programmers and home hobbyists who already know how to program in Java and who want to learn practical AI programming and information processing techniques. I have tried to make this an enjoyable book to work through. In the style of a “cook book,” the chapters can be studied in any order. Most chapters follow the same pattern: a motivation for learning a technique, some theory for the technique, and a Java example program that you can experiment with.

[My Java example programs for this book can be found on github](https://github.com/mark-watson/Java-AI-Book-Code) and can be used under both the LGPL3 and Apache 2 licenses - choose whichever of these two licenses that works best for you. Git pull request with code improvements will be appreciated by me and the readers of this book.

My goal is to teach you both the theory of common AI techniques and to provide you with Java source code to save you some time and effort. Even though I have worked almost exclusively in the field of deep learning in the last six years, I urge you, dear reader, to look at the field of AI as being far broader than machine learning and deep learning in particular. Just as it is wrong to consider the higher level fields of Category Theory or Group Theory to "be" mathematics, there is far more to AI than machine learning. Here we will take a more balanced view of AI, and indeed, my own current research is in hybrid AI, that is, the fusion of deep learning with good old fashioned symbolic AI, probabilistic reasoning, and explainability. 


I have been interested in AI since reading Bertram Raphael’s excellent book *Thinking Computer: Mind Inside Matter* in the early 1980s. I have also had the good fortune to work on many interesting AI projects including the development of commercial expert system tools for the Xerox LISP machines and the Apple Macintosh, development of commercial neural network tools, application of natural language and expert systems technology, medical information systems, application of AI technologies to Nintendo and PC video games, and the application of AI technologies to the financial markets. I have also applied statistical natural language processing techniques to analyzing social media data from Twitter and Facebook.

I enjoy AI programming, and hopefully this enthusiasm will also infect you the reader.

**Software Licenses for example programs in this book**

My example programs are licensed under the LGPL version 3 and/or Apache 2. I use several open source libraries and their licenses are:

-   PowerLoom Reasoning: LGPL
-   Jena Semantic Web: Apache 2

TBD: make sure last list is complete

**Acknowledgements**

I process the manuscript for this book using the [leanpub.com](http://leanpub.com) publishing system and I recommend leanpub.com to other authors. Write one manuscript and use leanpub.com to generate assets for PDF, iPad/iPhone, and Kindle versions.

I would like to thank Kevin Knight for writing a flexible framework for game search algorithms in *Common LISP* (Rich, Knight 1991) and for giving me permission to reuse his framework, rewritten in Java for some of the examples in the [Chapter on Search](#search). I would like to thank my
friend Tom Munnecke for my photo in this Preface. I have a library full of books on AI and I would like to thank the authors of all of these books for their influence on my professional life. I frequently reference books in the text that have been especially useful to me and that I recommend to my readers.

In particular, I would like to thank the authors of the following two books that have had the most influence on me:

-   Stuart Russell and Peter Norvig’s **Artificial Intelligence: A Modern Approach** which I consider to be the best single reference book for
    AI theory
-   John Sowa’s book **Knowledge Representation** is a resource that I frequently turn to for a holistic treatment of logic, philosophy, and knowledge representation in general

**Book Editor:**

Carol Watson

Thanks to the following people who found typos in this and earlier book editions: Carol Watson, James Fysh, Joshua Cranmer, Jack Marsh, Jeremy Burt, Jean-Marc Vanel
