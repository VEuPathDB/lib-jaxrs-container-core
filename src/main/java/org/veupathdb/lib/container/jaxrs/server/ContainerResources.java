package org.veupathdb.lib.container.jaxrs.server;

import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.controller.ApiDocService;
import org.veupathdb.lib.container.jaxrs.controller.HealthController;
import org.veupathdb.lib.container.jaxrs.controller.MetricsService;
import org.veupathdb.lib.container.jaxrs.middleware.*;
import org.veupathdb.lib.container.jaxrs.utils.db.DbManager;

/**
 * Container Meta Resources
 *
 * Universal services that should be available in all containerized services.
 *
 * This class is intended for framework internal use and is subject to change
 * with framework updates.
 */
@ApplicationPath("/")
abstract public class ContainerResources extends ResourceConfig {
  private static final Class<?>[] DEFAULT_CLASSES = {
    JacksonFilter.class,
    PrometheusFilter.class,
    RequestIdFilter.class,
    RequestLogger.class,

    ApiDocService.class,
    HealthController.class,
    MetricsService.class,
  };

  private boolean useExMap = true;

  public ContainerResources(Options opts) {
    registerClasses(DEFAULT_CLASSES);
    registerInstances(new AuthFilter(opts, DbManager.accountDatabase()));

    if (useExMap)
      register(ErrorMapper.class);

    for (var o : resources())
      if (o instanceof Class)
        register((Class<?>) o);
      else
        register(o);
  }

  /**
   * Disables the built in exception mapper.
   *
   * Call this if you are providing your own exception mapper implementation.
   */
  protected void disableExceptionMapper() {
    useExMap = false;
  }

  /**
   * Returns an array of JaxRS endpoints, providers, and contexts.
   *
   * Entries in the array can be either classes or instances.
   */
  abstract protected Object[] resources();
}
