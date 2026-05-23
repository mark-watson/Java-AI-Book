package com.knowledgegraphcreator;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class KgcTest {

  @Test
  void testKGC() throws IOException {
    Path outputFile = Path.of("output_with_duplicates.rdf");
    KGC client = new KGC("test_data/", outputFile.toString());

    assertTrue(Files.exists(outputFile), "Output RDF file should be created");

    String content = Files.readString(outputFile);
    assertFalse(content.isBlank(), "Output RDF file should not be empty");

    // Verify that known entity types from test data appear in the output
    assertTrue(content.contains("<http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"),
        "Output should contain RDF type triples");
    assertTrue(content.contains("<http://www.w3.org/1999/02/22-rdf-syntax-ns#/label>"),
        "Output should contain label triples");
  }
}
