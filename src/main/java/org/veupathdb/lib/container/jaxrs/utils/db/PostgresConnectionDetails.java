package org.veupathdb.lib.container.jaxrs.utils.db;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.db.pool.SimpleDbConfig;
import org.veupathdb.lib.container.jaxrs.config.DbOptions;

public class PostgresConnectionDetails extends RawConnectionDetails
{
  private static final int DEFAULT_PORT = 5432;

  private static final String JDBC_URL = "jdbc:postgresql://%s:%d/%s";

  private static final SupportedPlatform PLATFORM = SupportedPlatform.POSTGRESQL;

  public PostgresConnectionDetails(
    String host,
    int port,
    String dbName,
    String user,
    String pass,
    int poolSize
  ) {
    super(host, port, dbName, user, pass, poolSize);
  }

  @Override
  public String toJdbcString() {
    return String.format(JDBC_URL, host(), port(), dbName());
  }

  @Override
  public ConnectionPoolConfig toFgpUtilConfig() {
    return SimpleDbConfig.create(
      PLATFORM,
      toJdbcString(),
      user(),
      password(),
      poolSize()
    );
  }

  public static PostgresConnectionDetails fromOptions(final DbOptions opts) {
    return new PostgresConnectionDetails(
      opts.host().orElseThrow(),
      opts.port().orElse(DEFAULT_PORT),
      opts.name().orElseThrow(),
      opts.user().orElseThrow(),
      opts.pass().orElseThrow(),
      opts.poolSize().orElse(DbManager.DEFAULT_POOL_SIZE)
    );
  }
}
