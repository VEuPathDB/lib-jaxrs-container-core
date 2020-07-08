package org.veupathdb.lib.container.jaxrs.server.middleware;

import com.devskiller.friendly_id.FriendlyId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

import javax.annotation.Priority;
import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;

/**
 * Assigns a unique ID to each request for logging, error tracing purposes.
 */
@Provider
@Priority(0)
@PreMatching
public class RequestIdFilter
implements ContainerRequestFilter, ContainerResponseFilter {
  private final Logger log = LogManager.getLogger(RequestIdFilter.class);

  @Override
  public void filter(ContainerRequestContext req) {
    var id = FriendlyId.createFriendlyId();
    ThreadContext.put(Globals.CONTEXT_ID, id);
    req.setProperty(RequestKeys.REQUEST_ID, id);

    // At the end so it has the context id
    log.trace("RequestIdFilter#filter(req)");
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    log.trace("RequestIdFilter#filter(req, res)");
    ThreadContext.remove(Globals.CONTEXT_ID);
  }
}