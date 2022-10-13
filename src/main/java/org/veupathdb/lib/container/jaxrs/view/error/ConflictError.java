package org.veupathdb.lib.container.jaxrs.view.error;

public class ConflictError extends ErrorResponse
{
  public ConflictError() {
    super(ErrorStatus.CONFLICT);
  }

  public ConflictError(String message) {
    this();
    setMessage(message);
  }

  public ConflictError(Throwable err) {
    this(err.getMessage());
  }
}
