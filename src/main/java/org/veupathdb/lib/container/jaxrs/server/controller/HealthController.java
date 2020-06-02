package org.veupathdb.lib.container.jaxrs.server.controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.lang.management.ManagementFactory;

import org.veupathdb.lib.container.jaxrs.health.Dependency.Status;
import org.veupathdb.lib.container.jaxrs.health.ServerStatus;
import org.veupathdb.lib.container.jaxrs.providers.DependencyProvider;
import org.veupathdb.lib.container.jaxrs.utils.Threads;
import org.veupathdb.lib.container.jaxrs.view.health.ServiceHealth;
import org.veupathdb.lib.container.jaxrs.view.health.ServiceInfo;

@Path("/health")
public class HealthController {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public ServiceHealth getHealth() {
    var out = new ServiceHealth()
      .setStatus(ServerStatus.HEALTHY)
      .setInfo(new ServiceInfo()
        .setThreads(Threads.currentThreadCount())
        .setUptime(ManagementFactory.getRuntimeMXBean().getUptime()));

    var results = DependencyProvider.getInstance().testDependencies();

    results.stream()
      .filter(t -> t.status() != Status.ONLINE)
      .findAny()
      .ifPresent(__ -> out.setStatus(ServerStatus.UNHEALTHY));

    return out.setDependencies(results);
  }
}
