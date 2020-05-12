package org.veupathdb.lib.container.jaxrs.view.health;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.ArrayList;
import java.util.List;

import org.veupathdb.lib.container.jaxrs.health.Dependency.TestResult;
import org.veupathdb.lib.container.jaxrs.health.ServerStatus;

public class ServiceHealth {
  private List<DependencyHealth> dependencies;
  private ServerStatus status;
  private ServiceInfo info;

  @JsonGetter
  public List<DependencyHealth> getDependencies() {
    return dependencies;
  }

  public ServiceHealth setDependencies(List<TestResult> results) {
    dependencies = new ArrayList<>(results.size());
    results.stream()
      .map(DependencyHealth::new)
      .forEach(dependencies::add);
    return this;
  }

  @JsonGetter
  public ServerStatus getStatus() {
    return status;
  }

  public ServiceHealth setStatus(ServerStatus status) {
    this.status = status;
    return this;
  }

  @JsonGetter
  public ServiceInfo getInfo() {
    return info;
  }

  public ServiceHealth setInfo(ServiceInfo info) {
    this.info = info;
    return this;
  }
}
