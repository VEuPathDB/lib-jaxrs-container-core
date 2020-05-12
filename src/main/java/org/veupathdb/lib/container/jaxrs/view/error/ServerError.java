package org.veupathdb.lib.container.jaxrs.view.error;

public class ServerError extends ErrorResponse {
  public ServerError() {
    super(ErrorStatus.SERVER_ERROR);
  }

  public ServerError(String message) {
    this();
    setMessage(message);
  }

  public ServerError(Throwable err) {
    this(err.getMessage());
  }
}
