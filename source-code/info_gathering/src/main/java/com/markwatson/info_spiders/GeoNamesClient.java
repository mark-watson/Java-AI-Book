package com.markwatson.info_spiders;

import org.geonames.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright Mark Watson 2008-2020. All Rights Reserved.
 * License: Apache 2
 */

// You will need a free GeoNames account. Sign up:  https://www.geonames.org/login
// Then, set an environment variable: export GEONAMES=your-geonames-account-name

public class GeoNamesClient {
  public GeoNamesClient() {
  }

  private List<GeoNameData> helper(String name, String type) throws Exception {
    var ret = new ArrayList<GeoNameData>();

    String geonamesAccountName = System.getenv("GEONAMES");
    if (geonamesAccountName == null || geonamesAccountName.isBlank()) {
      throw new IllegalStateException("""
              GeoNames API key not configured.
              You will need a free GeoNames account.
              Sign up:  https://www.geonames.org/login
              Then, set an environment variable:
                   export GEONAMES=your-geonames-account-name""");
    }
    WebService.setUserName(geonamesAccountName);

    var searchCriteria = new ToponymSearchCriteria();
    searchCriteria.setStyle(Style.LONG);
    searchCriteria.setQ(name);
    ToponymSearchResult searchResult = WebService.search(searchCriteria);
    for (Toponym toponym : searchResult.getToponyms()) {
      if (toponym.getFeatureClassName() != null &&
        toponym.getFeatureClassName().contains(type) &&
        toponym.getName().contains(name) &&
        valid(toponym.getName())) {
        ret.add(new GeoNameData(toponym));
      }
    }
    return ret;
  }

  private boolean valid(String str) {
    return str.chars().noneMatch(Character::isDigit);
  }

  public List<GeoNameData> getCityData(String cityName) throws Exception {
    return helper(cityName, "city");
  }

  public List<GeoNameData> getCountryData(String countryName) throws Exception {
    return helper(countryName, "country");
  }

  public List<GeoNameData> getStateData(String stateName) throws Exception {
    List<GeoNameData> states = helper(stateName, "state");
    for (GeoNameData state : states) {
      state.geoType = GeoNameData.GeoType.STATE;
    }
    return states;
  }

  public List<GeoNameData> getRiverData(String riverName) throws Exception {
    return helper(riverName, "stream");
  }

  public List<GeoNameData> getMountainData(String mountainName) throws Exception {
    return helper(mountainName, "mountain");
  }
}
