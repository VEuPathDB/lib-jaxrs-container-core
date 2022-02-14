package org.veupathdb.lib.container.jaxrs.errors;

public class InitFailedException extends RuntimeException {
  private static final String message = "Server initialization failed.";

  public InitFailedException(Throwable cause) {
    super(message, cause);
  }
}
