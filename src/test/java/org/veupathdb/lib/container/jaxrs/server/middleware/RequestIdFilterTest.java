package org.veupathdb.lib.container.jaxrs.server.middleware;

import org.apache.logging.log4j.ThreadContext;
import org.glassfish.grizzly.http.server.Request;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import org.veupathdb.lib.container.jaxrs.utils.logging.LoggingVars;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RequestIdFilterTest {

  @Test
  void filter() {
    var request = mock(Request.class, Mockito.RETURNS_DEEP_STUBS);
    var requestCxt = mock(ContainerRequestContext.class);
    var test = new RequestIdFilter();

    test.doFilter(requestCxt, request);

    verify(requestCxt, times(1)).setProperty(eq(RequestKeys.REQUEST_ID), any(String.class));
    assertNotNull(ThreadContext.get(Globals.CONTEXT_ID));

    ThreadContext.remove(Globals.CONTEXT_ID);
    LoggingVars.clear();
  }

  @Test
  void testFilter() {
    var req = mock(ContainerRequestContext.class);
    var res = mock(ContainerResponseContext.class);
    var test = new RequestIdFilter();

    ThreadContext.put(Globals.CONTEXT_ID, "foo");

    test.filter(req, res);

    assertNull(ThreadContext.get(Globals.CONTEXT_ID));
  }
}
