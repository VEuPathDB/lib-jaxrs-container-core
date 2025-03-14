package org.veupathdb.lib.container.jaxrs.health;

import java.net.URI;

public class OAuthServiceDependency extends ServiceDependency{
  public OAuthServiceDependency(String url, int port) {
    super("OAuth Server", URI.create(url).getHost(), port);
  }

  @Override
  protected TestResult serviceTest() {
    // If we got here then the service is at least listening on the HTTPS
    // port.  Additional testing may be required to validate that the
    // server is actually behaving itself.
    return new TestResult(this, true, Status.ONLINE);
  }
}
