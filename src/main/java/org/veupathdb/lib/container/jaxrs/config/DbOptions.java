package org.veupathdb.lib.container.jaxrs.config;

import java.util.Optional;

import org.gusdb.fgputil.db.platform.SupportedPlatform;

public interface DbOptions {
  /**
   * Returns an option of a TNS Name for an Oracle database.
   */
  Optional<String> tnsName();

  /**
   * Returns an option of a host name for a database server.
   */
  Optional<String> host();

  /**
   * Returns an option of a port number for a database server.
   */
  Optional<Integer> port();

  /**
   * Returns an option of a name of a database to connect to.
   */
  Optional<String> name();

  /**
   * Returns an option of a database credentials username.
   */
  Optional<String> user();

  /**
   * Returns an option of a database credentials password.
   */
  Optional<String> pass();

  /**
   * Returns an option of a database platform type.
   */
  Optional<SupportedPlatform> platform();

  /**
   * Returns an option of a database connection pool size value.
   */
  Optional<Integer> poolSize();

  /**
   * Returns the display name for the database connection details.
   */
  String displayName();
}

