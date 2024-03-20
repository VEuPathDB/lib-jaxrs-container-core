package org.veupathdb.lib.container.jaxrs.utils;

import jakarta.ws.rs.core.HttpHeaders;

public final class RequestKeys {
  private RequestKeys() {}

  public static final String
    REQUEST_ID  = "request-id",
    AUTH_HEADER_LEGACY = "Auth-Key",
    ADMIN_TOKEN_HEADER = "admin-token",
    PROXIED_USER_ID_HEADER = "proxied-user-id",
    BEARER_TOKEN_HEADER = HttpHeaders.AUTHORIZATION,
    BEARER_TOKEN_QUERY_PARAM = "access_token"; // per https://www.rfc-editor.org/rfc/rfc6750#section-2.3

}
