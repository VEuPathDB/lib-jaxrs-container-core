package org.veupathdb.lib.container.jaxrs.health;

abstract public class AbstractDependency implements Dependency {

  protected final String name;

  protected AbstractDependency(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }
}
