package com.markwatson.ner_dbpedia;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TextToDbpediaUris Tests")
class TextToDbpediaUrisTest {

  @Test
  @DisplayName("Recognises known entities in a sentence")
  void recognisesKnownEntities() {
    String s = "PTL Satellite Network covered President Bill Clinton going to Guatemala and visiting the Coca Cola Company.";
    TextToDbpediaUris result = new TextToDbpediaUris(s);
    System.out.println(result);

    // Verify at least some expected entities were found
    assertFalse(result.personNames.isEmpty(),
        "Should find at least one person (e.g. Bill Clinton)");
    assertTrue(result.personNames.stream().anyMatch(n -> n.contains("Bill Clinton")),
        "Should recognise 'Bill Clinton' as a person");

    assertFalse(result.countryNames.isEmpty(),
        "Should find at least one country (e.g. Guatemala)");
    assertTrue(result.countryNames.stream().anyMatch(n -> n.contains("Guatemala")),
        "Should recognise 'Guatemala' as a country");
  }

  @Test
  @DisplayName("Empty input produces no entities")
  void emptyInputProducesNoEntities() {
    TextToDbpediaUris result = new TextToDbpediaUris("");
    assertTrue(result.personNames.isEmpty(), "No people in empty text");
    assertTrue(result.cityNames.isEmpty(), "No cities in empty text");
    assertTrue(result.companyNames.isEmpty(), "No companies in empty text");
    assertTrue(result.countryNames.isEmpty(), "No countries in empty text");
  }

  @Test
  @DisplayName("Unrecognised text produces no entities")
  void unrecognisedTextProducesNoEntities() {
    TextToDbpediaUris result = new TextToDbpediaUris("xyzzy foobar baz quux");
    assertTrue(result.personNames.isEmpty(), "No people in nonsense text");
    assertTrue(result.cityNames.isEmpty(), "No cities in nonsense text");
  }

  @Test
  @DisplayName("toString() produces readable output")
  void toStringIsReadable() {
    String s = "Bill Clinton visited Guatemala.";
    TextToDbpediaUris result = new TextToDbpediaUris(s);
    String output = result.toString();
    assertNotNull(output);
    assertTrue(output.contains("TextToDbpediaUris"),
        "toString should include class name");
  }
}
