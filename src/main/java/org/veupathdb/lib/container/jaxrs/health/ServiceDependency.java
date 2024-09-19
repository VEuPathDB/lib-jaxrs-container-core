package org.veupathdb.lib.container.jaxrs.health;


import org.slf4j.Logger;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

/**
 * Service Dependency
 * <p>
 * A dependency wrapper for an external HTTP service.
 * <p>
 * Implementations of this wrapper provide the specifics of how to check the
 * health status of the external service.
 */
abstract public class ServiceDependency extends ExternalDependency {

  private final Logger log;

  private final String url;
  private final int port;

  public ServiceDependency(String name, String url, int port) {
    super(name);
    this.url = url;
    this.port = port;
    this.log = LogProvider.logger(getClass());
  }

  public String getUrl() {
    return url;
  }

  public int getPort() {
    return port;
  }

  @Override
  public TestResult test() {
    log.info("Checking dependency health for external service {}", name);

    if (!pinger.isReachable(url, port))
      return new TestResult(this, false, Status.UNKNOWN);

    return serviceTest();
  }

  abstract protected TestResult serviceTest();

  @Override
  public void close() {}
}
