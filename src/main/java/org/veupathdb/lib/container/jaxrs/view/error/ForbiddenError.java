package org.veupathdb.lib.container.jaxrs.view.error;

public class ForbiddenError extends ErrorResponse
{
  public ForbiddenError() {
    super(ErrorStatus.FORBIDDEN);
  }

  public ForbiddenError(String message) {
    this();
    setMessage(message);
  }

  public ForbiddenError(Throwable err) {
    this(err.getMessage());
  }
}
