package org.veupathdb.lib.container.jaxrs.utils;

public final class RequestKeys {
  private RequestKeys() {}

  public static final String
    REQUEST_ID  = "request-id",
    AUTH_HEADER = "Auth-Key",
    ADMIN_TOKEN_HEADER = "Admin-Token",
    PROXIED_USER_ID = "proxied-user-id";
}
