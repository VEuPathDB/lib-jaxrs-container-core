package org.veupathdb.lib.container.jaxrs.utils;

public final class RequestKeys {
  private RequestKeys() {}

  public static final String
    REQUEST_ID  = "request-id",
    AUTH_HEADER = "Auth-Key",
    ADMIN_TOKEN_HEADER = "admin-token",
    PROXIED_USER_ID_HEADER = "proxied-user-id";
}
