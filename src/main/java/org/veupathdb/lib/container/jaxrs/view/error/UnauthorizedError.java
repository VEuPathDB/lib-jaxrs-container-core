package org.veupathdb.lib.container.jaxrs.view.error;

public class UnauthorizedError extends ErrorResponse {
  public UnauthorizedError() {
    super(ErrorStatus.UNAUTHORIZED);
  }

  public UnauthorizedError(String message) {
    this();
    setMessage(message);
  }

  public UnauthorizedError(Throwable err) {
    this(err.getMessage());
  }
}
