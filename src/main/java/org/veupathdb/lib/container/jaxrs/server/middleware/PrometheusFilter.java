package org.veupathdb.lib.container.jaxrs.server.middleware;

import com.devskiller.friendly_id.FriendlyId;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;

import io.prometheus.client.Summary;
import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.uri.UriTemplate;
import org.slf4j.Logger;
import org.slf4j.spi.LoggingEventBuilder;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

import java.io.IOException;
import java.util.List;

import static java.lang.String.format;

/**
 * Prometheus Metrics Filter
 * <p>
 * Collects and records metrics data about HTTP requests.  Data collected by
 * this filter is exposed by the <code>/metrics</code> endpoint.
 */
@Provider
@Priority(2)
public class PrometheusFilter
implements ContainerRequestFilter, ContainerResponseFilter, WriterInterceptor {
  private final Logger LOG = LogProvider.logger(PrometheusFilter.class);

  private static final String
      START_FORMAT = "Request start: %s %s",
      END_FORMAT   = "Request end: %s %s %d";

  static final String TIME_KEY = FriendlyId.createFriendlyId();
  static final String MATCHED_URL_KEY = "jerseyMatchedUrl";
  private static final String IS_ERROR_KEY = "isError";
  private static final String PATH_KEY = "path";
  private static final String STATUS_KEY = "status";
  private static final String METHOD_KEY = "method";

  private static final Counter reqCount = Counter.build()
    .name("http_total_requests")
    .help("Total HTTP request count.")
    .labelNames("path", "method", "status")
    .register();

  private static final Counter failedDuringResponseStream = Counter.build()
      .name("failed_during_response_stream")
      .help("Count of HTTP requests that failed while streaming results.")
      .labelNames("path", "method")
      .register();

  private static final Histogram reqTime = Histogram.build()
    .name("http_request_duration")
    .help("Request times in milliseconds")
    .labelNames("path", "method")
    .buckets(0.005, 0.01, 0.1, 0.5, 1, 5, 10, Double.POSITIVE_INFINITY)
    .register();

  /**
   * Capture a few quantiles to estimate service health. Quantiles are nice for a generic framework since different
   * services will have different latency distributions. Fixed bucket sizes are difficult to choose and the same set of
   * buckets may not be useful for all services.
   */
  private static final Summary reqTimeSummary = Summary.build()
    .name("http_request_duration_quantiles")
    .help("Request times in milliseconds")
    .labelNames("path", "method")
    .quantile(0.5, 0.05)
    .quantile(0.9, 0.03)
    .quantile(0.95, 0.01)
    .register();

  @Override
  public void filter(ContainerRequestContext req) {
    String path = req.getUriInfo().getPath();
    // use trace for metrics requests to avoid log congestion
    (path.equals("metrics") ? LOG.atTrace() : LOG.atDebug())
      .setMessage(() -> format(START_FORMAT, req.getMethod(), "/" + path))
      .log();

    String pathTemplate = getPathTemplate(req);

    req.setProperty(MATCHED_URL_KEY, pathTemplate);
    req.setProperty(
      TIME_KEY,
      reqTime.labels(
        getPathTemplate(req),
        req.getMethod()
      ).startTimer());
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    boolean hasResponseBody = res.getEntity() != null;
    boolean isError = res.getStatusInfo().getFamily() == Response.Status.Family.SERVER_ERROR;

    // check to see if this response has a body; if so, defer completion logging to the WriterInterceptor method
    if (!hasResponseBody) {
      logResponse(req.getUriInfo().getPath(), res.getStatus(), req.getMethod(), isError);
      double observedDuration = ((Timer) req.getProperty(TIME_KEY)).observeDuration();
      reqTimeSummary.labels(req.getUriInfo().getPath(), req.getMethod()).observe(observedDuration);
      req.removeProperty(TIME_KEY);
    } else {
      // If response body is present, set properties to be used in WriterInterceptor.
      req.setProperty(PATH_KEY, req.getUriInfo().getPath());
      req.setProperty(STATUS_KEY, res.getStatus());
      req.setProperty(METHOD_KEY, req.getMethod());
      req.setProperty(IS_ERROR_KEY, isError);
    }
    final String matchedUrlKey = (String) req.getProperty(MATCHED_URL_KEY);
    if (matchedUrlKey != null) {
      reqCount.labels(matchedUrlKey, req.getMethod(), String.valueOf(res.getStatus())).inc();
      // If we have a response body, we'll need to retain this key.
      if (!hasResponseBody) {
        req.removeProperty(MATCHED_URL_KEY);
      }
    }
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    try {
      // write the response
      context.proceed();
    }
    catch (Exception e) {
      failedDuringResponseStream.labels((String) context.getProperty(PATH_KEY), (String) context.getProperty(METHOD_KEY)).inc();
      throw e;
    }
    finally {
      String path = (String) context.getProperty(PATH_KEY);
      int status = (int) context.getProperty(STATUS_KEY);
      String method = (String) context.getProperty(METHOD_KEY);
      boolean isError = (boolean) context.getProperty(IS_ERROR_KEY);
      logResponse(path, status, method, isError);

      final String matchedUrlKey = (String) context.getProperty(MATCHED_URL_KEY);
      if (matchedUrlKey != null) {
        double observedDuration = ((Timer) context.getProperty(TIME_KEY)).observeDuration();
        reqTimeSummary.labels(matchedUrlKey, method).observe(observedDuration);
        context.removeProperty(TIME_KEY);
        context.removeProperty(MATCHED_URL_KEY);
      }
      context.removeProperty(PATH_KEY);
      context.removeProperty(STATUS_KEY);
      context.removeProperty(METHOD_KEY);
      context.removeProperty(IS_ERROR_KEY);
    }
  }

  private void logResponse(String path, int status, String method, boolean isError) {
    LoggingEventBuilder logLine;
    if (isError) {
      logLine = LOG.atWarn();
    } else if (path.equals("metrics")) {
      logLine = LOG.atTrace();
    } else {
      logLine = LOG.atDebug();
    }

    logLine.setMessage(() -> format(END_FORMAT, method, "/" + path, status)).log();
  }

  private String getPathTemplate(ContainerRequestContext request) {
    List<UriTemplate> uriTemplates = ((ContainerRequest)request).getUriInfo().getMatchedTemplates();
    if (uriTemplates != null && !uriTemplates.isEmpty()) {
      StringBuilder fullPath = new StringBuilder();
      for (UriTemplate uriTemplate : uriTemplates) {
        fullPath.insert(0, uriTemplate.getTemplate());
      }
      return fullPath.toString();
    }
    else {
      return "<unknown>";
    }
  }}
