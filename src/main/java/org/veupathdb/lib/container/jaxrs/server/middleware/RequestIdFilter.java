package org.veupathdb.lib.container.jaxrs.server.middleware;

import com.devskiller.friendly_id.FriendlyId;
import io.prometheus.client.Histogram;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.glassfish.grizzly.http.server.Request;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.utils.logging.LoggingVars;

import java.io.IOException;

/**
 * Assigns a unique ID to each request for logging, error tracing purposes.
 */
@Provider
@Priority(0)
@PreMatching
public class RequestIdFilter
implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {

  private static final Logger LOG = LogManager.getLogger(RequestIdFilter.class);

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
    ThreadContext.put(Globals.CONTEXT_ID, requestId);

    LoggingVars.setRequestThreadVars(requestId,
      request.getSession().getIdInternal(),
      request.getRemoteAddr());

    // At the end so it has the context id
    LOG.trace("RequestIdFilter#filter(requestCxt)");
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    LOG.trace("RequestIdFilter#aroundWriteTo(context)");
    try {
      // write the response
      context.proceed();
    }
    finally {
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
    ThreadContext.remove(Globals.CONTEXT_ID);
    LoggingVars.clear();
  }
}
