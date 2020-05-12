package org.veupathdb.lib.container.jaxrs.view.error;

import com.fasterxml.jackson.annotation.JsonGetter;

public class ErrorResponse {
  private final ErrorStatus status;
  private String message;

  public ErrorResponse(ErrorStatus status) {
    this.status = status;
  }

  @JsonGetter
  public String getStatus() {
    return status.toString();
  }

  @JsonGetter
  public String getMessage() {
    return message;
  }

  public ErrorResponse setMessage(String message) {
    this.message = message;
    return this;
  }
}
