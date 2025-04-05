package org.veupathdb.lib.container.jaxrs.utils.db;

import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.veupathdb.lib.container.jaxrs.config.DbOptions;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

public interface ConnectionDetails
{
  /**
   * Returns the host address for the connection.
   */
  String host();

  /**
   * Returns the host port for the connection.
   */
  int port();

  /**
   * Returns the connection credentials username.
   */
  String user();

  /**
   * Returns the connection credentials password.
   */
  String password();

  /**
   * Returns the name of the database.
   */
  String dbName();

  /**
   * Returns a JDBC connection string based on the current connection details.
   */
  String toJdbcString();

  /**
   * Returns the connection pool size for this connection.
   */
  int poolSize();

  /**
   * Returns an FgpUtil DatabaseInstance configuration based on the current
   * connection details.
   */
  ConnectionPoolConfig toFgpUtilConfig();

}
