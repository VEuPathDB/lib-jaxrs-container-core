package org.veupathdb.lib.container.jaxrs.view.error;

public class BadContentTypeError extends ErrorResponse
{
  public BadContentTypeError() {
    super(ErrorStatus.UNSUPPORTED_MEDIA);
  }

  public BadContentTypeError(String message) {
    this();
    setMessage(message);
  }

  public BadContentTypeError(Throwable error) {
    this(error.getMessage());
  }
}
