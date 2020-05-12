package org.veupathdb.lib.container.jaxrs.view.error;

public class InvalidInputError extends ErrorResponse {

  public InvalidInputError() {
    super(ErrorStatus.UNPROCESSABLE_ENTITY);
  }

  public InvalidInputError(String message) {
    this();
    setMessage(message);
  }

  public InvalidInputError(Throwable error) {
    this(error.getMessage());
  }
}
