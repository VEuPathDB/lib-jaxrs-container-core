package org.veupathdb.lib.container.jaxrs.view.error;

public class BadRequestError extends ErrorResponse
{
  public BadRequestError() {
    super(ErrorStatus.BAD_REQUEST);
  }

  public BadRequestError(String message) {
    this();
    setMessage(message);
  }

  public BadRequestError(Throwable error) {
    this(error.getMessage());
  }
}
