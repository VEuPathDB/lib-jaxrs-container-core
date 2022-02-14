package org.veupathdb.lib.container.jaxrs.server.middleware;

import java.util.function.Consumer;
import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Response.Status.Family;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

import static java.lang.String.format;

@Provider
@Priority(3)
@PreMatching
public class RequestLogger implements ContainerRequestFilter, ContainerResponseFilter {
  private static final String
    START_FORMAT = "Request start: %s %s",
    END_FORMAT   = "Request end: %s %s %d";

  private final Logger LOG = LogProvider.logger(RequestLogger.class);

  @Override
  public void filter(ContainerRequestContext req) {
    String path = req.getUriInfo().getPath();
    // use trace for metrics requests to avoid log congestion
    Consumer<Supplier<?>> fn = path.equals("metrics") ? LOG::trace : LOG::debug;
    fn.accept(() -> format(START_FORMAT, req.getMethod(), "/" + path));
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    boolean isError = res.getStatusInfo().getFamily() == Family.SERVER_ERROR;
    String path = req.getUriInfo().getPath();
    // use trace for metrics requests to avoid log congestion; warn for errors
    Consumer<Supplier<?>> fn = isError ? LOG::warn : path.equals("metrics") ? LOG::trace : LOG::debug;
    fn.accept(() -> format(END_FORMAT, req.getMethod(), "/" + path, res.getStatus()));
  }
}
