package org.veupathdb.lib.container.jaxrs.utils.logging;

import org.slf4j.MDC;

/**
 * Centralizes the assigning of log line vars (referenced in log4j2.yml patterns)
 */
public class LoggingVars {

  private static final int SHORTENED_ID_LENGTH = 5;

  private static final String
    SHORT_SESSION_ID = "sessionId",
    SHORT_REQUEST_ID = "requestId",
    REQUEST_START = "requestTimer",
    TRACE_ID = "traceId",
    IP_ADDRESS = "ipAddress";

  public static void setNonRequestThreadVars(String threadId) {
    setRequestThreadVars(threadId, threadId, "<no_ip_address>", threadId);
  }

  public static void setRequestThreadVars(
    String requestId,
    String sessionId,
    String ipAddress,
    String traceId
  ) {
    MDC.put(SHORT_REQUEST_ID, shorten(requestId));
    MDC.put(SHORT_SESSION_ID, shorten(sessionId));
    MDC.put(IP_ADDRESS, ipAddress);
    MDC.put(TRACE_ID, traceId);
    MDC.put(REQUEST_START, String.valueOf(System.currentTimeMillis()));
  }

  private static String shorten(String id) {
    return id == null ? "empty" : id.substring(0, Math.min(SHORTENED_ID_LENGTH, id.length()));
  }

  public static void clear() {
    MDC.remove(SHORT_REQUEST_ID);
    MDC.remove(SHORT_SESSION_ID);
    MDC.remove(IP_ADDRESS);
    MDC.remove(TRACE_ID);
    MDC.remove(REQUEST_START);
  }
}
