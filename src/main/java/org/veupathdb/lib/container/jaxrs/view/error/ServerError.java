package org.veupathdb.lib.container.jaxrs.view.error;

import com.fasterxml.jackson.annotation.JsonGetter;

public class ServerError extends ErrorResponse {
  private final String requestId;

  public ServerError(String rid) {
    super(ErrorStatus.SERVER_ERROR);
    this.requestId = rid;
  }

  public ServerError(String rid, String message) {
    this(rid);
    setMessage(message);
  }

  public ServerError(String rid, Throwable err) {
    this(rid, err.getMessage());
  }

  @JsonGetter
  public String getRequestId() {
    return requestId;
  }
}
