package org.spring.springboot.domain;

import java.util.List;

public class CityData extends BasicResult {
  
  private List<String> suggests;

  public List<String> getSuggests() {
    return suggests;
  }

  public void setSuggests(List<String> suggests) {
    this.suggests = suggests;
  }
}
