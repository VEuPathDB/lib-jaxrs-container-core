package org.veupathdb.lib.container.jaxrs.health;

import java.io.Closeable;
import java.sql.SQLException;
import javax.sql.DataSource;

import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

public class DatabaseDependency extends ExternalDependency
{
  private final String     url;
  private final int        port;
  private final DataSource ds;

  private String testQuery = "SELECT 1;";

  public DatabaseDependency(String name, String url, int port, DataSource ds) {
    super(name);
    this.url  = url;
    this.port = port;
    this.ds   = ds;
  }

  public void setTestQuery(String testQuery) {
    this.testQuery = testQuery;
  }

  @Override
  public TestResult test() {
    var log = LogProvider.logger(getClass());

    log.info("Checking dependency health for database {}", name);

    if (!pinger.isReachable(url, port))
      return new TestResult(this, false, Status.UNKNOWN);

    try (
      var con = ds.getConnection();
      var stm = con.createStatement()
    ) {
      stm.execute(testQuery);
      return new TestResult(this, true, Status.ONLINE);
    } catch (SQLException e) {
      log.warn("Health check failed for database {}", name);
      log.debug("Stacktrace", e);
      return new TestResult(this, true, Status.UNKNOWN);
    }
  }

  @Override
  public void close() throws Exception {
    if (ds instanceof Closeable)
      ((Closeable) ds).close();
    else if (ds instanceof AutoCloseable)
      ((AutoCloseable) ds).close();
  }
}
