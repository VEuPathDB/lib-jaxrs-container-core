package org.veupathdb.lib.container.jaxrs.view.health;

import com.fasterxml.jackson.annotation.JsonGetter;

import org.veupathdb.lib.container.jaxrs.health.Dependency.TestResult;

public class DependencyHealth {
  private final TestResult result;

  public DependencyHealth(TestResult result) {
    this.result = result;
  }

  @JsonGetter
  public String getName() {
    return result.dependency().getName();
  }

  @JsonGetter
  public boolean isReachable() {
    return result.reachable();
  }

  @JsonGetter
  public String getStatus() {
    return result.status().name().toLowerCase();
  }
}
