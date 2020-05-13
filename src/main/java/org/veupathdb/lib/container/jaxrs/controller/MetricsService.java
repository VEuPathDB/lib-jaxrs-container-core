package org.veupathdb.lib.container.jaxrs.controller;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

import java.io.OutputStreamWriter;

@Path("/metrics")
public class MetricsService {

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public StreamingOutput getMetrics() {
    return output -> {
      try (var write = new OutputStreamWriter(output)) {
        TextFormat.write004(write, CollectorRegistry.defaultRegistry.metricFamilySamples());
      }
    };
  }
}
