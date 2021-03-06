package org.veupathdb.lib.container.jaxrs.server.middleware;

import com.devskiller.friendly_id.FriendlyId;
import javax.annotation.Priority;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.glassfish.grizzly.http.server.Request;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.utils.logging.LoggingVars;

/**
 * Assigns a unique ID to each request for logging, error tracing purposes.
 */
@Provider
@Priority(0)
@PreMatching
public class RequestIdFilter
implements ContainerRequestFilter, ContainerResponseFilter {

  private static final Logger LOG = LogManager.getLogger(RequestIdFilter.class);

  @Inject
  private javax.inject.Provider<Request> _request;

  @Override
  public void filter(ContainerRequestContext req) {
    doFilter(req, _request.get());
  }

  public void doFilter(ContainerRequestContext requestCxt, Request request) {
    // generate and assign request id
    var requestId = FriendlyId.createFriendlyId();
    requestCxt.setProperty(RequestKeys.REQUEST_ID, requestId);
    _request.get().setAttribute(RequestKeys.REQUEST_ID, requestId);
    ThreadContext.put(Globals.CONTEXT_ID, requestId);

    LoggingVars.setRequestThreadVars(requestId,
      request.getSession().getIdInternal(),
      request.getRemoteAddr());

    // At the end so it has the context id
    LOG.trace("RequestIdFilter#filter(requestCxt)");
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    LOG.trace("RequestIdFilter#filter(req, res)");
    ThreadContext.remove(Globals.CONTEXT_ID);
    LoggingVars.clear();
  }
}
