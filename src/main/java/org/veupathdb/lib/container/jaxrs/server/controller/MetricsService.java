package org.veupathdb.lib.container.jaxrs.server.controller;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

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
