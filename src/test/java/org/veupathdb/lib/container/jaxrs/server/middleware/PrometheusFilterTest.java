package org.veupathdb.lib.container.jaxrs.server.middleware;

import io.prometheus.client.Histogram;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ExtendedUriInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.veupathdb.lib.container.jaxrs.server.middleware.PrometheusFilter.MATCHED_URL_KEY;
import static org.veupathdb.lib.container.jaxrs.server.middleware.PrometheusFilter.TIME_KEY;

class PrometheusFilterTest {

  @AfterEach
  void reset() throws Exception {
    var i = LogProvider.class.getDeclaredField("instance");
    i.setAccessible(true);
    i.set(null, null);
  }

  @Test
  void filter() throws Exception {
    var stat = mock(LogProvider.class);
    var log  = mock(Logger.class);
    var req  = mock(ContainerRequest.class);
    var uri  = mock(ExtendedUriInfo.class);

    when(stat.getLogger(PrometheusFilter.class)).thenReturn(log);
    when(req.getMethod()).thenReturn("");
    when(req.getUriInfo()).thenReturn(uri);
    when(uri.getPath()).thenReturn("");

    var i = LogProvider.class.getDeclaredField("instance");
    i.setAccessible(true);
    i.set(null, stat);

    new PrometheusFilter().filter(req);

    verify(stat, times(1)).getLogger(PrometheusFilter.class);
    verify(log, times(1)).debug((Supplier<?>)any());
  }

  @Test
  void testFilterDebug() throws Exception {
    var stat = mock(LogProvider.class);
    var log  = mock(Logger.class);
    var req  = mock(ContainerRequestContext.class);
    var res  = mock(ContainerResponseContext.class);
    var uri  = mock(UriInfo.class);
    var timer = mock(Histogram.Timer.class);

    when(stat.getLogger(PrometheusFilter.class)).thenReturn(log);
    when(req.getMethod()).thenReturn("");
    when(req.getUriInfo()).thenReturn(uri);
    when(uri.getPath()).thenReturn("");
    when(res.getStatusInfo()).thenReturn(Status.OK);
    when(res.getStatus()).thenReturn(0);
    when(req.getProperty(TIME_KEY)).thenReturn(timer);
    when(req.getProperty(MATCHED_URL_KEY)).thenReturn("url");
    when(timer.observeDuration()).thenReturn(100.0);

    var i = LogProvider.class.getDeclaredField("instance");
    i.setAccessible(true);
    i.set(null, stat);

    new PrometheusFilter().filter(req, res);

    verify(timer, times(1)).observeDuration();
    verify(stat, times(1)).getLogger(PrometheusFilter.class);
    verify(log, times(1)).debug((Supplier<?>) any());
  }


  @Test
  @SuppressWarnings("unchecked")
  void testFilterWarn() throws Exception {
    var stat = mock(LogProvider.class);
    var log  = mock(Logger.class);
    var req  = mock(ContainerRequestContext.class);
    var res  = mock(ContainerResponseContext.class);
    var uri  = mock(UriInfo.class);
    var timer = mock(Histogram.Timer.class);

    when(stat.getLogger(PrometheusFilter.class)).thenReturn(log);
    when(req.getMethod()).thenReturn("");
    when(req.getUriInfo()).thenReturn(uri);
    when(uri.getPath()).thenReturn("");
    when(res.getStatusInfo()).thenReturn(Status.INTERNAL_SERVER_ERROR);
    when(res.getStatus()).thenReturn(0);
    when(req.getProperty(TIME_KEY)).thenReturn(timer);
    when(req.getProperty(MATCHED_URL_KEY)).thenReturn("url");

    var i = LogProvider.class.getDeclaredField("instance");
    i.setAccessible(true);
    i.set(null, stat);

    new PrometheusFilter().filter(req, res);

    ArgumentCaptor<Supplier<?>> capt = ArgumentCaptor.forClass(Supplier.class);

    verify(stat, times(1)).getLogger(PrometheusFilter.class);
    verify(log, times(1)).warn(capt.capture());
    assertTrue(capt.getValue().get() instanceof String);
  }
}
