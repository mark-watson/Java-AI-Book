package com.markwatson.info_spiders;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for GeoNamesClient.
 */
class GeoNamesClientTest {

  @Test
  @DisplayName("Fetch and display geographic data for cities, countries, states, rivers, and mountains")
  void testFetchAndDisplay() {
    var client = new GeoNamesClient();

    // Skip the test if GeoNames service is down or rate-limited
    try {
      List<GeoNameData> check = client.getCityData("Paris");
      org.junit.jupiter.api.Assumptions.assumeTrue(check != null && !check.isEmpty(),
          "GeoNames service is currently rate-limited or returned no results. Skipping test.");
    } catch (Exception e) {
      System.err.println("WARNING: GeoNames service is currently unavailable: " + e.getMessage());
      org.junit.jupiter.api.Assumptions.assumeTrue(false,
          "GeoNames service is currently unavailable. Skipping test.");
    }

    try {
      List<GeoNameData> cities = client.getCityData("Paris");
      System.out.println(cities);
      assertFalse(cities.isEmpty(), "Should find at least one result for 'Paris'");
      pause();

      List<GeoNameData> countries = client.getCountryData("Canada");
      System.out.println(countries);
      assertFalse(countries.isEmpty(), "Should find at least one result for 'Canada'");
      pause();

      List<GeoNameData> states = client.getStateData("California");
      System.out.println(states);
      assertFalse(states.isEmpty(), "Should find at least one result for 'California'");
      for (GeoNameData state : states) {
        assertEquals(GeoNameData.GeoType.STATE, state.geoType, "State data should have STATE geo type");
      }
      pause();

      List<GeoNameData> rivers = client.getRiverData("Amazon");
      System.out.println(rivers);
      assertFalse(rivers.isEmpty(), "Should find at least one result for 'Amazon'");
      pause();

      List<GeoNameData> mountains = client.getMountainData("Whitney");
      System.out.println(mountains);
      assertFalse(mountains.isEmpty(), "Should find at least one result for 'Whitney'");
    } catch (Exception e) {
      fail("GeoNames API call failed: " + e.getMessage(), e);
    }
  }

  private static void pause() {
    try {
      Thread.sleep(2000);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }
}
