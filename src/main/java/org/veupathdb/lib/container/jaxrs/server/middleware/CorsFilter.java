package org.veupathdb.lib.container.jaxrs.server.middleware;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

/**
 * CORS Header Filter
 * <p>
 * Appends CORS headers to server responses indicating to the client that this
 * server accepts cross origin requests.
 */
public class CorsFilter implements ContainerResponseFilter
{
  private static final String[][] CORS_HEADERS = {
    {"Access-Control-Allow-Origin", "*"},
    {"Access-Control-Allow-Headers", "*"},
    {"Access-Control-Allow-Credentials", "true"},
    {"Access-Control-Allow-Methods", "*"},
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
