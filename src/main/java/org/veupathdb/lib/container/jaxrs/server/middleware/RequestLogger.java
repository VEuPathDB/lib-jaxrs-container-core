package org.veupathdb.lib.container.jaxrs.server.middleware;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;

import javax.annotation.Priority;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Response.Status.Family;
import javax.ws.rs.ext.Provider;

import java.util.function.Consumer;

import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

import static java.lang.String.format;

@Provider
@Priority(3)
@PreMatching
public class RequestLogger implements ContainerRequestFilter, ContainerResponseFilter {
  private static final String
    START_FORMAT = "Request start: {} {}",
    END_FORMAT   = "Request end: %s %s %d";

  private final Logger LOG = LogProvider.logger(RequestLogger.class);

  @Override
  public void filter(ContainerRequestContext req) {
    LOG.debug(START_FORMAT, req.getMethod(), "/" + req.getUriInfo().getPath());
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    final Consumer<Supplier<?>> fn;
    if (res.getStatusInfo().getFamily() == Family.SERVER_ERROR) {
      fn = LOG::warn;
    } else {
      fn = LOG::debug;
    }

    fn.accept(() -> format(END_FORMAT, req.getMethod(),
      "/" + req.getUriInfo().getPath(), res.getStatus()));
  }
}
