package com.markwatson.info_spiders;

import org.geonames.Toponym;

/**
 * Copyright Mark Watson 2008-2020. All Rights Reserved.
 * License: Apache-2.0
 */
public class GeoNameData {
  public enum GeoType {
    CITY, COUNTRY, STATE, RIVER, MOUNTAIN, UNKNOWN
  }

  public int geoNameId = 0;
  public GeoType geoType = GeoType.UNKNOWN;
  public String name = "";
  public double latitude = 0;
  public double longitude = 0;
  public String countryCode = "";

  public GeoNameData(Toponym toponym) {
    geoNameId = toponym.getGeoNameId();
    latitude = toponym.getLatitude();
    longitude = toponym.getLongitude();
    name = toponym.getName();
    countryCode = toponym.getCountryCode();
    String featureClassName = toponym.getFeatureClassName();
    if (featureClassName != null) {
      geoType = switch (featureClassName) {
        case String s when s.startsWith("city") -> GeoType.CITY;
        case String s when s.startsWith("country") -> GeoType.COUNTRY;
        case String s when s.startsWith("state") -> GeoType.STATE;
        case String s when s.startsWith("stream") -> GeoType.RIVER;
        case String s when s.startsWith("mountain") -> GeoType.MOUNTAIN;
        default -> GeoType.UNKNOWN;
      };
    }
  }

  public GeoNameData() {
  }

  @Override
  public String toString() {
    return "[GeoNameData: %s, type: %s, country code: %s, ID: %d, latitude: %.4f, longitude: %.4f]"
            .formatted(name, geoType, countryCode, geoNameId, latitude, longitude);
  }
}
