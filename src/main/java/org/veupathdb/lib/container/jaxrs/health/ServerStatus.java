package org.veupathdb.lib.container.jaxrs.health;

public enum ServerStatus {
  HEALTHY,
  UNHEALTHY;

  @Override
  public String toString() {
    return name().toLowerCase();
  }
}
