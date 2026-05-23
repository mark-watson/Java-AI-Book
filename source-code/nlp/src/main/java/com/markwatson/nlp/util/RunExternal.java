package com.markwatson.nlp.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Utility to run an external process and capture its output.
 * Uses ProcessBuilder (modern replacement for Runtime.exec()).
 */
public class RunExternal {

  public static void main(String[] argv) {
    try {
      var pb = new ProcessBuilder("sh", "-c",
          "echo \"thhe dogg brked\" | /usr/local/bin/aspell -a list");
      pb.redirectErrorStream(true);
      var p = pb.start();
      try (var input = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line;
        while ((line = input.readLine()) != null) {
          System.out.println(line);
        }
      }
      p.waitFor();
    } catch (Exception err) {
      err.printStackTrace();
    }
  }
}
