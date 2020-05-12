package org.veupathdb.lib.container.jaxrs.health;

/**
 * Service Dependency
 * <p>
 * A wrapper for external resources providing methods needed for performing
 * health checks.
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

  class TestResult {
    private Dependency dependency;
    private final boolean reachable;
    private final Status status;

    public TestResult(Dependency dep, boolean reachable, Status status) {
      this.dependency = dep;
      this.reachable = reachable;
      this.status = status;
    }

    public Dependency dependency() {
      return dependency;
    }

    public boolean isReachable() {
      return reachable;
    }

    public Status status() {
      return status;
    }
  }
}
