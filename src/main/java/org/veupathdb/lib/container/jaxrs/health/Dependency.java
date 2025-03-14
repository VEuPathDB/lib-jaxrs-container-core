package org.veupathdb.lib.container.jaxrs.health;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.Map;

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

  class TestResult {
    public final Dependency dependency;

    public final boolean reachable;

    public final Status status;

    public final Map<String, Object> additionalInfo;

    public TestResult(Dependency dependency, boolean reachable, Status status) {
      this(dependency, reachable, status, Collections.emptyMap());
    }

    public TestResult(Dependency dependency, boolean reachable, Status status, Map<String, Object> addtlInfo) {
      this.dependency = dependency;
      this.reachable = reachable;
      this.status = status;
      this.additionalInfo = addtlInfo;
    }
  }
}
