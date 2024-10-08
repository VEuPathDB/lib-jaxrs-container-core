package org.veupathdb.lib.container.jaxrs.server.middleware;

import com.devskiller.friendly_id.FriendlyId;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.glassfish.grizzly.http.server.Request;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.utils.logging.LoggingVars;

import java.io.IOException;
import java.util.Optional;

/**
 * Assigns a unique ID to each request for logging, error tracing purposes.
 */
@Provider
@Priority(0)
@PreMatching
public class RequestIdFilter
implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

  private static final Logger LOG = LogProvider.logger(RequestIdFilter.class);

  @Inject
  jakarta.inject.Provider<Request> _request;

  @Override
  public void filter(ContainerRequestContext req) {
    doFilter(req, _request.get());
  }

  public void doFilter(ContainerRequestContext requestCxt, Request request) {
    // generate and assign request id
    var requestId = FriendlyId.createFriendlyId();
    requestCxt.setProperty(RequestKeys.REQUEST_ID, requestId);
    request.setAttribute(RequestKeys.REQUEST_ID, requestId);

    MDC.put(Globals.CONTEXT_ID, requestId);

    final String traceId = Optional.ofNullable(request.getHeader(Globals.TRACE_ID_HEADER))
        .orElse(FriendlyId.createFriendlyId());

    LoggingVars.setRequestThreadVars(requestId,
        request.getSession().getIdInternal(),
        request.getRemoteAddr(),
        traceId);
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    LOG.trace("RequestIdFilter#aroundWriteTo(context)");
    try {
      // write the response
      context.proceed();
    } finally {
      removeContext();
    }
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    LOG.trace("RequestIdFilter#filter(req, res)");
    // Remove request ID from thread context only if we have no response body.
    // If we have a response body rely on aroundWriteTo to remove from thread context.
    if (!res.hasEntity()) {
      removeContext();
    }
  }

  private void removeContext() {
    MDC.remove(Globals.CONTEXT_ID);
    LoggingVars.clear();
  }
}
