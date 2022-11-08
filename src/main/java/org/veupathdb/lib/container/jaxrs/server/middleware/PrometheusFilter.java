package org.veupathdb.lib.container.jaxrs.server.middleware;

import com.devskiller.friendly_id.FriendlyId;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.ext.WriterInterceptor;
import jakarta.ws.rs.ext.WriterInterceptorContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.uri.UriTemplate;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

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

  private static Function<String, String> PathTransform = Function.identity();

  private static final Counter reqCount = Counter.build()
    .name("http_total_requests")
    .help("Total HTTP request count.")
    .labelNames("path", "method", "status")
    .register();

  private static final Histogram reqTime = Histogram.build()
    .name("http_request_duration")
    .help("Request times in milliseconds")
    .labelNames("path", "method")
    .buckets(0.005, 0.01, 0.1, 0.5, 1, 5, 10, Double.POSITIVE_INFINITY)
    .register();

  @Override
  public void filter(ContainerRequestContext req) {
    String path = req.getUriInfo().getPath();
    // use trace for metrics requests to avoid log congestion
    Consumer<Supplier<?>> fn = path.equals("metrics") ? LOG::trace : LOG::debug;
    fn.accept(() -> format(START_FORMAT, req.getMethod(), "/" + path));
    String pathTemplate = getPathTemplate(req);
    req.setProperty(MATCHED_URL_KEY, pathTemplate);
    LOG.info("Matched template: " + getPathTemplate(req) + ". Starting timer with path a label.");
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
      ((Timer) req.getProperty(TIME_KEY))
          .observeDuration();
      req.removeProperty(TIME_KEY);
    } else {
      // If response body is present, set properties to be used in WriterInterceptor.
      req.setProperty(PATH_KEY, req.getUriInfo().getPath());
      req.setProperty(STATUS_KEY, res.getStatus());
      req.setProperty(METHOD_KEY, req.getMethod());
      req.setProperty(IS_ERROR_KEY, isError);
    }
    reqCount.labels((String) req.getProperty(MATCHED_URL_KEY), req.getMethod(), String.valueOf(res.getStatus()))
      .inc();
    req.removeProperty(MATCHED_URL_KEY);
  }

  @Override
  public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
    try {
      // write the response
      context.proceed();
    }
    finally {
      String path = (String) context.getProperty(PATH_KEY);
      int status = (int) context.getProperty(STATUS_KEY);
      String method = (String) context.getProperty(METHOD_KEY);
      boolean isError = (boolean) context.getProperty(IS_ERROR_KEY);
      logResponse(path, status, method, isError);
      context.removeProperty(PATH_KEY);
      context.removeProperty(STATUS_KEY);
      context.removeProperty(METHOD_KEY);
      context.removeProperty(IS_ERROR_KEY);

      LOG.info("Observing duration with request body.");
      ((Timer) context.getProperty(TIME_KEY))
          .observeDuration();
      context.removeProperty(TIME_KEY);
    }
  }

  private void logResponse(String path, int status, String method, boolean isError) {
    // use trace for metrics requests to avoid log congestion; warn for errors
    Consumer<Supplier<?>> fn = isError ? LOG::warn : path.equals("metrics") ? LOG::trace : LOG::debug;
    fn.accept(() -> format(END_FORMAT, method, "/" + path, status));
  }

  /**
   * Set custom URL path transformation to apply to paths being recorded in the
   * response timings.
   *
   * This can be useful in stripping out variables from the URL that may pollute
   * the prometheus metrics.
   *
   * For example, it may be desired that the following paths be recorded as one
   * metric:
   *
   * <pre>
   *   /users/123/preferences
   *   /users/234/preferences
   *   /users/345/preferences
   * </pre>
   *
   * For metrics purposes it would be best to strip out the user ID from the
   * path before recording.  For example, the above paths could be transformed
   * to the following to get a merged metric:
   *
   * <pre>
   *   /users/{user-id}/preferences
   * </pre>
   *
   * @param fn Function used to transform the path before recording it in the
   *           request/response time metrics.
   */
  public static void setPathTransform(Function<String, String> fn) {
    PathTransform = fn;
  }

  private String getPathTemplate(ContainerRequestContext request) {
    List<UriTemplate> uriTemplates = ((ContainerRequest)request).getUriInfo().getMatchedTemplates();
    if (uriTemplates != null && !uriTemplates.isEmpty()) {
      String fullPath = "";
      for (UriTemplate uriTemplate : uriTemplates) {
        fullPath = uriTemplate.getTemplate() + fullPath;
      }
      return fullPath;
    }
    else {
      return "<unknown>";
    }
  }}
