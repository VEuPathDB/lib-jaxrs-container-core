package org.veupathdb.lib.container.jaxrs.server;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.server.controller.ApiDocService;
import org.veupathdb.lib.container.jaxrs.server.controller.HealthController;
import org.veupathdb.lib.container.jaxrs.server.controller.MetricsService;
import org.veupathdb.lib.container.jaxrs.server.middleware.*;

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
    CustomResponseHeadersFilter.class,

    ApiDocService.class,
    HealthController.class,
    MetricsService.class,
    ErrorMapper.class
  };

  private final Options opts;

  public ContainerResources(Options opts) {
    this.opts = opts;
    registerClasses(DEFAULT_CLASSES);

    for (var o : resources())
      if (o instanceof Class)
        register((Class<?>) o);
      else
        register(o);
  }

  /**
   * Enable cross origin request allowance headers.
   */
  public void enableCors() {
    register(CorsFilter.class);
  }

  /**
   * Enable authentication checks for annotated resources.
   * <p>
   * Note: The authentication feature requires both the AccountDB and UserDB to
   * be enabled.
   * </p>
   */
  public void enableAuth() {
    registerInstances(new AuthFilter(opts));
  }

  /**
   * Enable Jersey trace response headers.
   */
  public void enableJerseyTrace() {
    property("jersey.config.server.tracing.type", "ALL");
    property("jersey.config.server.tracing.threshold", "VERBOSE");
  }

  /**
   * Enable dummy authentication.
   *
   * WARNING:
   *  * Do not use this with the regular auth filter
   *  * this is for test/dev purposes only
   */
  public void enableDummyAuth() {
    register(DummyAuthFilter.class);
  }

  /**
   * Returns an array of JaxRS endpoints, providers, and contexts.
   *
   * Entries in the array can be either classes or instances.
   */
  abstract protected Object[] resources();
}
