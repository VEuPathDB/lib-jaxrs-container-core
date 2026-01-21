package org.veupathdb.lib.container.jaxrs.health;

import java.sql.SQLException;

import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

/**
 * Database Dependency
 * <p>
 * Dependency wrapper for a database instance.
 */
public class FgpDatabaseDependency extends ExternalDependency
{
  private final String url;
  private final int port;
  private final DatabaseInstance ds;
  private final String testQuery;

  public FgpDatabaseDependency(
    String name,
    String url,
    int port,
    DatabaseInstance ds,
    String testQuery
  ) {
    super(name);
    this.ds = ds;
    this.url = url;
    this.port = port;
    this.testQuery = testQuery;
  }

  @Override
  public TestResult test() {
    // Get log here to include request context.
    var log = LogProvider.logger(getClass());

    log.info("Checking dependency health for database {}", name);

    if (!pinger.isReachable(url, port))
      return new TestResult(this, false, Status.UNKNOWN);

    try (
      var con = ds.getDataSource().getConnection();
      var stmt = con.createStatement()
    ) {
      stmt.execute(testQuery);
      return new TestResult(this, true, Status.ONLINE);
    } catch (SQLException e) {
      log.warn("Health check failed for database {}", name);
      log.debug("Stacktrace", e);
      return new TestResult(this, true, Status.UNKNOWN);
    }
  }

  @Override
  public void close() throws Exception {
    ds.close();
  }
}
