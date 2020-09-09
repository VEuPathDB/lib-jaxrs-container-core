package org.veupathdb.lib.container.jaxrs.view.error;

public class NotFoundError extends ErrorResponse
{
  public NotFoundError() {
    super(ErrorStatus.NOT_FOUND);
  }

  public NotFoundError(String message) {
    this();
    setMessage(message);
  }

  public NotFoundError(Throwable error) {
    this(error.getMessage());
  }
}
