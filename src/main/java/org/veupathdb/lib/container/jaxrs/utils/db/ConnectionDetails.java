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

  /**
   * Returns a {@link ConnectionDetails} instance from the input options.
   */
  static ConnectionDetails fromOptions(final DbOptions opts) {
    final var log = LogProvider.logger(ConnectionDetails.class);
    log.debug("Setting up connection for db " + opts.displayName());

    if (opts.platform().isPresent()) {
      log.debug("Platform provided.");
      return switch (opts.platform().get()) {
        case ORACLE -> {
          log.debug("Using Oracle.");
          yield OracleConnectionDetails.fromOptions(opts);
        }
        case POSTGRESQL -> {
          log.debug("Using PostgreSQL.");
          yield PostgresConnectionDetails.fromOptions(opts);
        }
      };
    }

    log.debug("Platform not provided, defaulting to Oracle.");
    // If no platform is specified, default to Oracle
    return OracleConnectionDetails.fromOptions(opts);
  }
}
