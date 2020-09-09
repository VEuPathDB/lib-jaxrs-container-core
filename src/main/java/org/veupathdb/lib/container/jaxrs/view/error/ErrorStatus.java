package org.veupathdb.lib.container.jaxrs.view.error;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ErrorStatus
{
  BAD_REQUEST          ("bad-request"),
  UNAUTHORIZED         ("unauthorized"),
  FORBIDDEN            ("forbidden"),
  NOT_FOUND            ("not-found"),
  BAD_METHOD           ("method-not-allowed"),
  UNSUPPORTED_MEDIA    ("unsupported-content-type"),
  TEAPOT               ("I'm a teapot."),
  UNPROCESSABLE_ENTITY ("invalid-input"),
  SERVER_ERROR         ("server-error");

  final String value;

  ErrorStatus(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return value;
  }
}
