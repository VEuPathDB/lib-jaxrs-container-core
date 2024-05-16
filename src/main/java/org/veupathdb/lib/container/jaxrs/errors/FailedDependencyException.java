package org.veupathdb.lib.container.jaxrs.errors;

import jakarta.ws.rs.ClientErrorException;

public class FailedDependencyException extends ClientErrorException {
  public static final int STATUS_CODE = 424;

  private final String dependency;

  public FailedDependencyException(String dependency, String message) {
    super(message, STATUS_CODE);
    this.dependency = dependency;
  }

  public FailedDependencyException(String dependency, String message, Throwable cause) {
    super(message, STATUS_CODE, cause);
    this.dependency = dependency;
  }

  public String getDependency() {
    return dependency;
  }
}
