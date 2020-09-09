package org.veupathdb.lib.container.jaxrs.view.error;

public class BadMethodError extends ErrorResponse
{
  public BadMethodError() {
    super(ErrorStatus.BAD_METHOD);
  }

  public BadMethodError(String message) {
    this();
    setMessage(message);
  }

  public BadMethodError(Throwable err) {
    this(err.getMessage());
  }
}
