package org.veupathdb.lib.container.jaxrs.view.error;

public class GoneError extends ErrorResponse
{
  public GoneError() {
    super(ErrorStatus.GONE);
  }

  public GoneError(String message) {
    this();
    setMessage(message);
  }

  public GoneError(Throwable err) {
    this(err.getMessage());
  }
}
