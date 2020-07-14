package org.veupathdb.lib.container.jaxrs.server.middleware;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

/**
 * CORS Header Filter
 * <p>
 * Appends CORS headers to server responses indicating to the client that this
 * server accepts cross origin requests.
 */
public class CorsFilter implements ContainerResponseFilter
{
  /**
   * Headers that may be included in cross origin requests.
   */
  private static final String[] ALLOWED_HEADERS = {
    "Origin",
    "Content-Type",
    "Accept",
    "Authorization",
    "Auth-Key"
  };

  /**
   * Methods that may be used for cross origin requests.
   */
  private static final String[] ALLOWED_METHODS = {
    "GET",
    "POST",
    "PUT",
    "DELETE",
    "OPTIONS",
    "HEAD"
  };

  private static final String[][] CORS_HEADERS = {
    {"Access-Control-Allow-Origin", "*"},
    {"Access-Control-Allow-Headers", String.join(", ", ALLOWED_HEADERS)},
    {"Access-Control-Allow-Credentials", "true"},
    {"Access-Control-Allow-Methods", String.join(", ", ALLOWED_METHODS)},
  };

  @Override
  public void filter(
    final ContainerRequestContext req,
    final ContainerResponseContext res
  ) {
    LogProvider.logger(CorsFilter.class).trace("CorsFilter#filter(req, res)");
    var head = res.getHeaders();
    for (var pair : CORS_HEADERS)
      head.add(pair[0], pair[1]);
  }
}
