package org.veupathdb.lib.container.jaxrs.health;

/**
 * Service Dependency
 * <p>
 * A wrapper for anything the server depends on to be considered in a "healthy"
 * state.
 */
public interface Dependency extends AutoCloseable {

  enum Status {ONLINE, UNKNOWN, OFFLINE}

  /**
   * Get the unique name of this dependency
   */
  String getName();

  /**
   * Test the resource availability
   */
  TestResult test();

  record TestResult(Dependency dependency, boolean reachable, Status status) {}
}
