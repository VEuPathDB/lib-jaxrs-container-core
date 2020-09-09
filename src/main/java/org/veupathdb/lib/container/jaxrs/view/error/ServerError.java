package org.veupathdb.lib.container.jaxrs.view.error;

import com.fasterxml.jackson.annotation.JsonGetter;

public class ServerError extends ErrorResponse
{
  public static final String JSON_KEY_REQUEST_ID = "requestId";

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

  @JsonGetter(JSON_KEY_REQUEST_ID)
  public String getRequestId() {
    return requestId;
  }
}
