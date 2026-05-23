package com.markwatson.nlp;

import public_domain.Stemmer;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.markwatson.nlp.util.NameValue;

/**
 * Associate pre-trained classification categories (tags) with input text: assigns
 * categories for news story types, technology category types, social information
 * types, etc. to input text.
 *
 * <p/>
 * Copyright 1998-2012 by Mark Watson. All rights reserved.
 * <p/>
 * This software is can be used under either of the following licenses:
 * <p/>
 * 1. LGPL v3<br/>
 * 2. Apache 2
 * <p/>
 */
public class AutoTagger {
    private static Map<String, Map<String, Float>> tagClasses;
    private static String[] tagClassNames;
    private static final List<Map<String, Float>> hashes = new ArrayList<>();
    /**
     * 
     * Static initialization of data from an XML file that contains
     * word count statistics for several common topics
     * 
     */
    static {
        DefaultHandler handler = new TagsSAXHandler();        
        SAXParserFactory factory = SAXParserFactory.newInstance();  // Use the default non-validating parser
        try (var xmlInputStream = new FileInputStream(System.getProperty("user.dir") + "/" + "test_data/classification_tags.xml")) {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(xmlInputStream, handler);
        } catch (IOException | ParserConfigurationException | SAXException e) {
            e.printStackTrace();
        }
        tagClassNames = new String[tagClasses.size()];
        int count = 0;
        for (var cname : tagClasses.keySet()) {
            System.out.println("cname=" + cname);
            hashes.add(tagClasses.get(cname));
            tagClassNames[count++] = cname;
        }
        tagClasses = null;
    }

    public AutoTagger() {
    }
    
    public List<NameValue<String, Float>> getTags(String text) {
    	var results = new ArrayList<NameValue<String, Float>>();
        List<SFtriple> tag_data = getTagsHelper(text);
        for (SFtriple triple : tag_data) {
        	results.add(new NameValue<>(triple.s(), triple.f()));
        }
    	return results;
    }
    
    /**
     * 
     * @param text input text processed to identify categories
     * @return
     */
    private List<SFtriple> getTagsHelper(String text) {
        var stemmer = new Stemmer();
        List<String> stems = stemmer.stemString(text);
        return getTagsHelper(stems);
    }

    /**
     * 
     * @param stems
     * @return
     */
    private List<SFtriple> getTagsHelper(List<String> stems) {
        var ret = new ArrayList<SFtriple>();
        int size = tagClassNames.length;
        float[] scores = new float[size];
        for (String stem : stems) {
            for (int i = 0; i < size; i++) {
                Float f = hashes.get(i).get(stem);
                if (f != null) scores[i] += f;
            }
        }
        float max_score = 0.001f;
        for (int i = 0; i < size; i++) if (max_score < scores[i]) max_score = scores[i];
        float cutoff = 0.2f * max_score;
        for (int i = 0; i < size; i++) {
            if (scores[i] > cutoff) ret.add(new SFtriple(tagClassNames[i], scores[i] / max_score, i));
        }
        //for (int i=0; i<size; i++) System.out.println(tagClassNames[i]+"\t"+scores[i]);
        ret.sort(Comparator.comparingDouble(SFtriple::f).reversed());
        return ret;
    }

    /**
     * 
     * @param text
     * @return
     */
    float[] getWordImportanceWeights(String text) {
        List<String> stems = new Stemmer().stemString(text);
        List<SFtriple> best_tags = getTagsHelper(stems);
        return getWordImportanceWeights(stems, best_tags);
    }
    /**
     * 
     * @param stems
     * @return
     */
    float[] getWordImportanceWeights(List<String> stems) {
        List<SFtriple> best_tags = getTagsHelper(stems);
        return getWordImportanceWeights(stems, best_tags);
    }
    /**
     * Find the words that are most important for determining tags and use
     * this information to find which words in input text are most important for
     * summarization, semantic understanding, etc.
     * @param stems  stems for words in text
     * @param best_tags  the best tags for this text
     * @return
     */
    private float[] getWordImportanceWeights(List<String> stems, List<SFtriple> best_tags) {
        int num = stems.size();
        float[] ret = new float[num];
        float scale = 1.0f / best_tags.size();
        for (SFtriple tag : best_tags) {
            Map<String, Float> h = hashes.get(tag.topicIndex());
            for (int i = 0; i < num; i++) {
                Float f = h.get(stems.get(i));
                if (f != null) ret[i] += f * scale;
            }
        }
        return ret;
    }
    
    /**
     * Test program
     * 
     * @param args not used
     */
    public static void main(String[] args) {
    	var test = new AutoTagger();
    	var results = test.getTags("The President went to Congress to argue for his tax bill before leaving on a vacation to Las Vegas to see some shows and gamble.");
    	for (var result : results) {
    		System.out.println(result);
    	}
    }

    static class TagsSAXHandler extends org.xml.sax.helpers.DefaultHandler {
        int depth = 0;
        String last_topic = "";
        Map<String, Float> hash;
        // override default methods for a few SAX events:
        @Override
        public void startElement (String uri, String localName,
                                  String qName, Attributes attributes)
            throws SAXException
        {
            if (depth == 0) {
                tagClasses = new HashMap<>();
            }
            if (depth == 1)  {
                last_topic = attributes.getValue(0);
                hash = new HashMap<>();
                tagClasses.put(last_topic, hash);
            }
            if (depth == 2) {
                hash.put(attributes.getValue(0), Float.parseFloat(attributes.getValue(1)));
            }

            // debug:
            /*for (int i=0; i<depth; i++) System.out.print("   ");
            System.out.println("" + depth + " element: " + qName);
            if (attributes != null) {
                int num = attributes.getLength();
                for (int i=0; i<num; i++) {
                    String name = attributes.getQName(i);
                    String value = attributes.getValue(i);
                    for (int k=0; k<depth; k++) System.out.print("   ");
                    System.out.println("    attribute: " + name + " value: " + value);
                }
            }*/


            depth++;
        }
        @Override
        public void endElement (String uri, String localName, String qName)
            throws SAXException
        {
            depth--;
        }
        @Override
        public void characters (char[] ch, int start, int length)
            throws SAXException
        {
        }
    }
    
    /**
     * A scored tag triple: tag name, score, and topic index.
     * Implemented as a Java record for conciseness.
     */
    record SFtriple(String s, float f, int topicIndex) implements Comparable<SFtriple> {

        @Override
        public String toString() { return "[SFtriple: " + s + " : " + f + " : " + topicIndex + "]"; }

        @Override
        public int compareTo(SFtriple o) {
            return Float.compare(o.f, f);
        }
    }

}
