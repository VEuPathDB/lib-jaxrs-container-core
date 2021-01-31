package org.veupathdb.lib.container.jaxrs.utils.logging;

import org.apache.logging.log4j.ThreadContext;

/**
 * Centralizes the assigning of log line vars (referenced in log4j2.yml patterns)
 */
public class LoggingVars {

  private static final int SHORTENED_ID_LENGTH = 5;

  private static final String
    SHORT_SESSION_ID = "sessionId",
    SHORT_REQUEST_ID = "requestId",
    REQUEST_START = "requestTimer",
    IP_ADDRESS = "ipAddress";

  public static void setNonRequestThreadVars(String threadId) {
    setRequestThreadVars(threadId, threadId, "<no_ip_address>");
  }

  public static void setRequestThreadVars(String requestId, String sessionId, String ipAddress) {
    ThreadContext.put(SHORT_REQUEST_ID, shorten(requestId));
    ThreadContext.put(SHORT_SESSION_ID, shorten(sessionId));
    ThreadContext.put(IP_ADDRESS, ipAddress);
    ThreadContext.put(REQUEST_START, String.valueOf(System.currentTimeMillis()));
  }

  private static String shorten(String id) {
    return id == null ? "empty" : id.substring(0, Math.min(SHORTENED_ID_LENGTH, id.length()));
  }

  public static void clear() {
    ThreadContext.remove(SHORT_REQUEST_ID);
    ThreadContext.remove(SHORT_SESSION_ID);
    ThreadContext.remove(IP_ADDRESS);
    ThreadContext.remove(REQUEST_START);
  }
}
