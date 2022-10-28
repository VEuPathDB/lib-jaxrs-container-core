package org.veupathdb.lib.container.jaxrs.server.middleware;

import com.devskiller.friendly_id.FriendlyId;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;

import java.util.function.Function;

/**
 * Prometheus Metrics Filter
 * <p>
 * Collects and records metrics data about HTTP requests.  Data collected by
 * this filter is exposed by the <code>/metrics</code> endpoint.
 */
@Provider
@Priority(2)
@PreMatching
public class PrometheusFilter
implements ContainerRequestFilter, ContainerResponseFilter {

  private static Function<String, String> PathTransform = Function.identity();

  private static final String TIME_KEY = FriendlyId.createFriendlyId();

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
    req.setProperty(
      TIME_KEY,
      reqTime.labels(
        getSubstitutedPath(req),
        req.getMethod()
      ).startTimer());
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    ((Timer) req.getProperty(TIME_KEY))
      .observeDuration();
    reqCount.labels(getSubstitutedPath(req), req.getMethod(), String.valueOf(res.getStatus()))
      .inc();
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

  private static String getSubstitutedPath(ContainerRequestContext req) {
    var path = PathTransform.apply(req.getUriInfo().getPath());
    var vars = req.getUriInfo().getPathParameters();

    for (var entry : vars.entrySet())
      for (var value : entry.getValue())
        path = path.replace(value, entry.getKey());

    return path;
  }
}
