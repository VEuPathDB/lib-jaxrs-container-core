package org.veupathdb.lib.container.jaxrs.view.health;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonGetter;

import org.veupathdb.lib.container.jaxrs.health.Dependency.TestResult;

import java.util.Map;

public class DependencyHealth {
  private final TestResult result;

  public DependencyHealth(TestResult result) {
    this.result = result;
  }

  @JsonGetter
  public String getName() {
    return result.dependency.getName();
  }

  @JsonGetter
  public boolean isReachable() {
    return result.reachable;
  }

  @JsonGetter
  public String getStatus() {
    return result.status.name().toLowerCase();
  }

  @JsonAnyGetter
  public Map<String, Object> getExtraFields() {
    return result.additionalInfo;
  }
}
