# Combining the WordNet Linguistic Database With OpenNLP

Here we build on the material from the last chapter by using OpenNLP to process input text to identify parts of speech and then looking up words with their parts of speech in WordNet.

We only need course part of speech (POS) tagging. WordNet uses the word types:

    NOUN(1, "n", "noun"),
    VERB(2, "v", "verb"),
    ADJECTIVE(3, "a", "adjective"),
    ADVERB(4, "r", "adverb");

