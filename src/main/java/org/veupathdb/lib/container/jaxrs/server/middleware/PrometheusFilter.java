package org.veupathdb.lib.container.jaxrs.server.middleware;

import com.devskiller.friendly_id.FriendlyId;
import io.prometheus.client.Counter;
import io.prometheus.client.Histogram;
import io.prometheus.client.Histogram.Timer;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.*;
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
        PathTransform.apply(req.getUriInfo().getPath()),
        req.getMethod()
      ).startTimer());
  }

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {

    ((Timer) req.getProperty(TIME_KEY))
      .observeDuration();

    var path = PathTransform.apply(req.getUriInfo().getPath());
    var vars = req.getUriInfo().getPathParameters();

    for (var entry : vars.entrySet())
      for (var value : entry.getValue())
        path = path.replace(value, entry.getKey());

    reqCount.labels(path, req.getMethod(), String.valueOf(res.getStatus()))
      .inc();
  }

  public static void setPathTransform(Function<String, String> fn) {
    PathTransform = fn;
  }
}
