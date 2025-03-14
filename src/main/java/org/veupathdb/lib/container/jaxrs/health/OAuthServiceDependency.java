package org.veupathdb.lib.container.jaxrs.health;

public class OAuthServiceDependency extends ServiceDependency{
  public OAuthServiceDependency(String url, int port) {
    super("OAuth Server", trimURL(url), port);
  }

  @Override
  protected TestResult serviceTest() {
    // If we got here then the service is at least listening on the HTTPS
    // port.  Additional testing may be required to validate that the
    // server is actually behaving itself.
    return new TestResult(this, true, Status.ONLINE);
  }

  private static String trimURL(String url) {
    return url.startsWith("https://")
      ? url.substring(8)
      : (url.startsWith("http://")
        ? url.substring(7)
        : url);
  }
}
