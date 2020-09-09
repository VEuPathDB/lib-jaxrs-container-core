package org.veupathdb.lib.container.jaxrs.view.error;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonInclude;

public class ErrorResponse {
  public static final String
    JSON_KEY_STATUS  = "status",
    JSON_KEY_MESSAGE = "message";

  private final ErrorStatus status;

  private String message;

  public ErrorResponse(ErrorStatus status) {
    this.status = status;
  }

  @JsonGetter(JSON_KEY_STATUS)
  public String getStatus() {
    return status.toString();
  }

  @JsonGetter(JSON_KEY_MESSAGE)
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  public String getMessage() {
    return message;
  }

  public ErrorResponse setMessage(String message) {
    this.message = message;
    return this;
  }
}
