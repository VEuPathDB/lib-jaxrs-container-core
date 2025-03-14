package org.veupathdb.lib.container.jaxrs.server;

import org.glassfish.jersey.server.ResourceConfig;

import jakarta.ws.rs.ApplicationPath;

import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.health.OAuthServiceDependency;
import org.veupathdb.lib.container.jaxrs.health.ServiceDependency;
import org.veupathdb.lib.container.jaxrs.providers.DependencyProvider;
import org.veupathdb.lib.container.jaxrs.server.middleware.CorsFilter;
import org.veupathdb.lib.container.jaxrs.server.controller.ApiDocService;
import org.veupathdb.lib.container.jaxrs.server.controller.HealthController;
import org.veupathdb.lib.container.jaxrs.server.controller.MetricsService;
import org.veupathdb.lib.container.jaxrs.server.middleware.*;
import org.veupathdb.lib.jaxrs.raml.multipart.MultipartApplicationEventListener;
import org.veupathdb.lib.jaxrs.raml.multipart.MultipartMessageBodyReader;

/**
 * Container Meta Resources
 * <p>
 * Universal services that should be available in all containerized services.
 * <p>
 * This class is intended for framework internal use and is subject to change
 * with framework updates.
 */
@ApplicationPath("/")
abstract public class ContainerResources extends ResourceConfig {
  private static final Class<?>[] DEFAULT_CLASSES = {
    MultipartApplicationEventListener.class,
    MultipartMessageBodyReader.class,

    JacksonFilter.class,
    PrometheusFilter.class,
    RequestIdFilter.class,
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

    if (opts.getCorsEnabled())
      enableCors();

    for (var o : resources())
      if (o instanceof Class)
        register((Class<?>) o);
      else
        register(o);
  }

  /**
   * Enable cross-origin request allowance headers.
   */
  public void enableCors() {
    register(CorsFilter.class);
  }

  /**
   * Enable OAuth authentication checks for annotated resources.
   * <p>
   * Enabling this feature requires that the OAuth config options are set.  If
   * those options are not provided the service will fail to start up.
   */
  public void enableAuth() {
    registerInstances(new AuthFilter(opts));
    DependencyProvider.getInstance().register(new OAuthServiceDependency(opts.getOAuthUrl().orElseThrow(), 443));
  }

  /**
   * Enable Jersey trace response headers.
   */
  public void enableJerseyTrace() {
    property("jersey.config.server.tracing.type", "ALL");
    property("jersey.config.server.tracing.threshold", "VERBOSE");
  }

  /**
   * Returns an array of JaxRS endpoints, providers, and contexts.
   * <p>
   * Entries in the array can be either classes or instances.
   */
  abstract protected Object[] resources();
}
