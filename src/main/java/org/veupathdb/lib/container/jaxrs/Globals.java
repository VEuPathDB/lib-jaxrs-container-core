package org.veupathdb.lib.container.jaxrs;

public final class Globals {
  private Globals() {}

  @Deprecated
  public static final String DB_ACCOUNT_SCHEMA = "useraccounts.";

  public static final String
    CONTEXT_ID = "context-id",
    REQUEST_USER = "user-profile",
    TRACE_ID_HEADER = "traceid";

  @Deprecated
  public static final String X_CONTEXT_ID = "X-Context-Id";
}
