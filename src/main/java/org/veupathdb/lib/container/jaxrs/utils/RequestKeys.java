package org.veupathdb.lib.container.jaxrs.utils;

public final class RequestKeys {
  private RequestKeys() {}

  public static final String
    REQUEST_ID  = "request-id",
    AUTH_HEADER_LEGACY = "Auth-Key",
    ADMIN_TOKEN_HEADER = "Admin-Token";
}
