package com.markwatson.nlp;

import com.markwatson.nlp.util.ScoredList;
import com.markwatson.nlp.util.Tokenizer;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wrapper for code to find both human and place names in input text.
 * 
 * <p/>
 * Copyright 2002-2008 by Mark Watson. All rights reserved.
 * <p/>
 * <p/>
 * Copyright 1998-2012 by Mark Watson. All rights reserved.
 * <p/>
 * This software is can be used under either of the following licenses:
 * <p/>
 * 1. LGPL v3<br/>
 * 2. Apache 2
 * <p/>
 */
public class ExtractNames {
    /**
     *        Facade method: get all place and human names from a text string
     * @param words
     * @return 
     */
    public ScoredList[] getProperNames(List<String> words) {
        var placeNames = new ScoredList();
        var humanNames = new ScoredList();
        ScoredList[] ret = new ScoredList[2];
        ret[0] = humanNames; ret[1] = placeNames;
        if (words == null) return ret;
        for (int i = 0; i < words.size(); i++) {
            // 5 word human names:
            if (isHumanName(words, i, 5)) {
                String s = String.join(" ", words.subList(i, i + 5));
                humanNames.addValue(s);
                i += 4;
                continue;
            }
            // 4 word human names:
            if (isHumanName(words, i, 4)) {
                String s = String.join(" ", words.subList(i, i + 4));
                humanNames.addValue(s);
                i += 3;
                continue;
            }
            // 3 word names:
            if (isPlaceName(words, i, 3)) {
                String s = String.join(" ", words.subList(i, i + 3));
                placeNames.addValue(s);
                i += 2;
                continue;
            }
            if (isHumanName(words, i, 3)) {
                String s = String.join(" ", words.subList(i, i + 3));
                humanNames.addValue(s);
                i += 2;
                continue;
            }
            // 2 word names:
            if (isPlaceName(words, i, 2)) {
                String s = words.get(i) + " " + words.get(i + 1);
                placeNames.addValue(s);
                i += 1;
                continue;
            }
            if (isHumanName(words, i, 2)) {
                String s = words.get(i) + " " + words.get(i + 1);
                humanNames.addValue(s);
                i += 1;
                continue;
            }
            // 1 word names:
            if (isPlaceName(words, i, 1)) {
                placeNames.addValue(words.get(i));
                continue;
            }
        }
        return ret;
    }
    /**
     * 
     * @param s
     * @return
     */
    public ScoredList[] getProperNames(String s) {
        List<String> words = Tokenizer.wordsToList(s);
        return getProperNames(words);
    }

    /**
     * 
     * @param words
     * @param startIndex
     * @param numWords
     * @return
     */
    public boolean isPlaceName(List<String> words, int startIndex, int numWords) {
        if ((startIndex + numWords) > words.size())  return false;
        if (numWords == 1) return isPlaceName(words.get(startIndex));
        String s = String.join(" ", words.subList(startIndex, startIndex + numWords));
        return isPlaceName(s);
    }

    /**
     * 
     * @param name
     * @return
     */
    public boolean isPlaceName(String name) {
        if (placeNameHash.get(name) != null) System.out.println("* place name: " + name + ", placeNameHash.get(name): " + placeNameHash.get(name));
        return placeNameHash.get(name) != null;
    }

    /**
     * 
     * @param s
     * @return
     */
    public boolean isHumanName(String s) {
        List<String> ss = Tokenizer.wordsToList(s);
        //System.out.print("Tokens: "); for (int i=0; i<ss.length; i++) System.out.print(ss[i] + " "); System.out.println();
        if (ss == null)  return false;
        return isHumanName(ss);
    }


    /**
     * 
     * @param words
     * @param index
     * @param numWords
     * @return
     */
    public boolean isHumanName(List<String> words, int index, int numWords) {
        if ((index + numWords) > words.size())  return false;
        return isHumanName(words.subList(index, index + numWords));
    }

    /**
     * 
     * @param words
     * @return
     */
    public boolean isHumanName(List<String> words) {
        int len = words.size();
        if (len == 1) {
            if (lastNameHash.get(words.get(0)) != null) return true;
        } else if (len == 2) {
            if (firstNameHash.get(words.get(0)) != null && lastNameHash.get(words.get(1)) != null) return true;
            if (prefixHash.get(words.get(0))    != null && lastNameHash.get(words.get(1)) != null) return true;
        } else if (len == 3) {
            if (firstNameHash.get(words.get(0)) != null &&
                firstNameHash.get(words.get(1)) != null &&
                lastNameHash.get(words.get(2)) != null) return true;
            if (prefixHash.get(words.get(0)) != null &&
                firstNameHash.get(words.get(1)) != null &&
                lastNameHash.get(words.get(2)) != null) return true;
            if (prefixHash.get(words.get(0)) != null &&
                words.get(1).equals(".") &&
                lastNameHash.get(words.get(2)) != null) return true;
        } else if (len == 4) {
            if (firstNameHash.get(words.get(0)) != null &&
                firstNameHash.get(words.get(1)) != null &&
                firstNameHash.get(words.get(2)) != null &&
                lastNameHash.get(words.get(3)) != null) return true;
            if (firstNameHash.get(words.get(0)) != null &&
                words.get(1).length() == 1 &&
                words.get(2).equals(".") &&
                lastNameHash.get(words.get(3)) != null) return true;
            if (prefixHash.get(words.get(0)) != null &&
                firstNameHash.get(words.get(1)) != null &&
                firstNameHash.get(words.get(2)) != null &&
                lastNameHash.get(words.get(3)) != null) return true;
            if (prefixHash.get(words.get(0)) != null &&
                firstNameHash.get(words.get(1)) != null &&
                words.get(2).length() == 1 &&
                lastNameHash.get(words.get(3)) != null) return true;
        } else if (len == 5) {
            if (firstNameHash.get(words.get(0)) != null &&
                firstNameHash.get(words.get(1)) != null &&
                words.get(2).length() == 1 &&
                words.get(3).equals(".") &&
                lastNameHash.get(words.get(4)) != null) return true;
            if (prefixHash.get(words.get(0)) != null &&
                firstNameHash.get(words.get(1)) != null &&
                words.get(2).length() == 1 &&
                words.get(3).equals(".") &&
                lastNameHash.get(words.get(4)) != null) return true;
        }
        return false;
    }

    /**
     * 
     */
    public ExtractNames() {
        this("test_data/propername.ser");
    }

    /**
     * 
     * @param dataPath
     */
    @SuppressWarnings("unchecked")
    public ExtractNames(String dataPath) {
        if (lastNameHash != null) return; // static data already loaded
        try {
            InputStream tempIns =
                this.getClass().getClassLoader().getResourceAsStream(dataPath);
            if (tempIns == null) {
                try {
                    tempIns = new FileInputStream(dataPath);
                } catch (java.io.FileNotFoundException e) {
                    throw new IllegalStateException(
                        "\ncom.knowledgebooks.entity_extraction.Names: failed to open '" + dataPath + "'\n", e);
                }
            }
            try (InputStream ins = tempIns) {
                var p = new ObjectInputStream(ins);
                lastNameHash = (Map<String, String>) p.readObject();
                firstNameHash = (Map<String, String>) p.readObject();
                placeNameHash = (Map<String, String>) p.readObject();
                prefixHash = (Map<String, String>) p.readObject();
            }
            
            // Write out human-readable name files for inspection
            writeNameFile("lastnames.txt", lastNameHash);
            writeNameFile("firstnames.txt", firstNameHash);
            writePlaceNameFile("placenames.txt", placeNameHash);
            writeNameFile("prefixnames.txt", prefixHash);
        } catch (Exception ee) {
            ee.printStackTrace();
        }
        System.out.println("# last names=" + lastNameHash.size() + ", # first names=" + firstNameHash.size());
    }

    /**
     * Helper to write out name hash keys to a text file.
     */
    private void writeNameFile(String filename, Map<String, ?> hash) {
        try (var fos = new FileOutputStream(filename);
             var out = new OutputStreamWriter(fos)) {
            for (String key : hash.keySet()) {
                out.write(key + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper to write out place names with cleanup for special characters.
     */
    private void writePlaceNameFile(String filename, Map<String, ?> hash) {
        try (var fos = new FileOutputStream(filename);
             var out = new OutputStreamWriter(fos)) {
            for (var entry : hash.entrySet()) {
                String key = entry.getKey();
                int idx;
                if ((idx = key.indexOf(';')) != -1) key = key.substring(0, idx);
                if ((idx = key.indexOf('(')) != -1) key = key.substring(0, idx);
                if ((idx = key.indexOf(',')) != -1) key = key.substring(0, idx);
                key = key.trim();
                out.write(key + ":" + entry.getValue() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * @param args
     */
    static public void main(String[] args) {
        var extractNames = new ExtractNames();
        // initialize everything, before printing any output - trying to see what is taking so long!
        if (args.length > 0) {
            ScoredList[] ret = extractNames.getProperNames(args[0]);
            System.out.println("Human names: " + ret[0].getValuesAsString());
            System.out.println("Place names: " + ret[1].getValuesAsString());
        } else {
            extractNames.isPlaceName("Paris");
            extractNames.isHumanName("President Bush");
            extractNames.isHumanName("President George Bush");
            extractNames.isHumanName("President George W. Bush");
            System.out.println("Initialization complete....");
            System.out.println("Paris: " + extractNames.isPlaceName("Paris"));
            System.out.println("Mexico: " + extractNames.isPlaceName("Mexico"));
            System.out.println("Fresno: " + extractNames.isPlaceName("Fresno"));
            System.out.println("Moscow: " + extractNames.isPlaceName("Moscow"));
            System.out.println("France: " + extractNames.isPlaceName("France"));
            System.out.println("Los Angeles: " + extractNames.isPlaceName("Los Angeles"));
            System.out.println("President Bush: " + extractNames.isHumanName("President Bush"));
            System.out.println("President George Bush: " + extractNames.isHumanName("President George Bush"));
            System.out.println("President George W. Bush: " + extractNames.isHumanName("President George W. Bush"));
            System.out.println("George W. Bush: " + extractNames.isHumanName("George W. Bush"));
            System.out.println("Senator Barbara Boxer: " + extractNames.isHumanName("Senator Barbara Boxer"));
            System.out.println("King Smith: " + extractNames.isHumanName("King Smith"));
            ScoredList[] ret = extractNames.getProperNames("George Bush played golf. President George W. Bush went to London England, Paris France and Mexico to see Mary Smith in Moscow. President Bush will return home Monday.");
            System.out.println("Human names: " + ret[0].getValuesAsString());
            System.out.println("Place names: " + ret[1].getValuesAsString());
            System.out.println("\n\n\n");
            
            // for book example:
            var names = new ExtractNames();
            System.out.println("Los Angeles: " +
                names.isPlaceName("Los Angeles"));
    System.out.println("President Bush: " +
                names.isHumanName("President Bush"));           
    System.out.println("President George Bush: " +
           names.isHumanName("President George Bush"));
    System.out.println("President George W. Bush: " +
           names.isHumanName("President George W. Bush"));
    ScoredList[] ret1 = names.getProperNames(
          "George Bush played golf. President  George W. Bush went to London England, Paris France and Mexico to see Mary  Smith in Moscow. President Bush will return home Monday.");
    System.out.println("Human names: " +
                       ret1[0].getValuesAsString());
    System.out.println("Place names: " +
                       ret1[1].getValuesAsString());
        }
    }

    static Map<String, String> lastNameHash = null;
    static Map<String, String> firstNameHash = null;
    static Map<String, String> placeNameHash = null; // cache for database access
    static Map<String, String> prefixHash = null;

}
