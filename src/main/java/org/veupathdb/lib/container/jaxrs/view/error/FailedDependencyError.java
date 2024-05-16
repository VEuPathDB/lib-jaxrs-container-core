package org.veupathdb.lib.container.jaxrs.view.error;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.veupathdb.lib.container.jaxrs.errors.FailedDependencyException;

public class FailedDependencyError extends ErrorResponse {
  public static final String JSON_KEY_DEPENDENCY = "dependency";

  private final String dependency;

  public FailedDependencyError(Throwable cause) {
    super(ErrorStatus.FAILED_DEPENDENCY);

    dependency = cause instanceof FailedDependencyException
      ? ((FailedDependencyException) cause).getDependency()
      : "unknown";

    setMessage(cause.getMessage());
  }

  public FailedDependencyError(String dependency, String message) {
    super(ErrorStatus.FAILED_DEPENDENCY);
    this.dependency = dependency;
    setMessage(message);
  }

  public FailedDependencyError(String dependency, Throwable cause) {
    this(dependency, cause.getMessage());
  }

  @JsonGetter(JSON_KEY_DEPENDENCY)
  public String getDependency() {
    return dependency;
  }
}
