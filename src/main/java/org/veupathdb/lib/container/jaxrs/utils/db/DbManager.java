package org.veupathdb.lib.container.jaxrs.utils.db;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.pool.SimpleDbConfig;

import java.util.Objects;

import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.health.DatabaseDependency;
import org.veupathdb.lib.container.jaxrs.providers.DependencyProvider;

/**
 * Database Manager.
 *
 * Handles the connection to DatabaseInstances and provides singleton access to
 * them if needed.
 */
public class DbManager {
  private DbManager() {}

  private static DbManager instance;

  public static DbManager getInstance() {
    if (instance == null)
      instance = new DbManager();

    return instance;
  }

  private static final String ERR_NOT_INIT = "Database.get%sDatabase() was"
    + " called before the database connection was initialized.";

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Oracle Related Constants                                          ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/


  /**
   * Default Oracle DB port
   */
  static final int ORACLE_PORT = 1521;

  /**
   * Oracle connection string template
   */
  private static final String ORACLE_URL = "jdbc:oracle:oci:@//%s:%d/%s";


  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Postgres Related Constants                                        ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/


  /**
   * Default Postgres DB port
   */
  static final int POSTGRES_PORT = 5432;

  /**
   * Postgres connection string template.
   */
  private static final String POSTGRES_URL = "jdbc:postgresql://%s:%d/%s";


  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    General Constants                                                 ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  static final SupportedPlatform DEFAULT_PLATFORM = SupportedPlatform.ORACLE;
  static final int DEFAULT_POOL_SIZE = 20;

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Database Instances                                                ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  private DatabaseInstance acctDb;
  private DatabaseInstance appDb;
  private DatabaseInstance userDb;


  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Account Database                                                  ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  public DatabaseInstance newAccountDatabase(Options opts) {
    if (Objects.nonNull(acctDb))
      return acctDb;

    var dbOpts = DbOptions.forAccountDB(opts);

    acctDb = new DatabaseInstance(initDbConfig(dbOpts));

    DependencyProvider.getInstance()
      .register(new DatabaseDependency("account-db", dbOpts.host, dbOpts.port,
        acctDb));

    return acctDb;
  }

  public DatabaseInstance newApplicationDatabase(Options opts) {
    if (Objects.nonNull(appDb))
      return appDb;

    var dbOpts = DbOptions.forAppDB(opts);

    appDb = new DatabaseInstance(initDbConfig(dbOpts));

    DependencyProvider.getInstance()
      .register(new DatabaseDependency("account-db", dbOpts.host, dbOpts.port,
        appDb));

    return appDb;
  }


  public DatabaseInstance newUserDatabase(Options opts) {
    if (Objects.nonNull(userDb))
      return userDb;

    var dbOpts = DbOptions.forAccountDB(opts);

    userDb = new DatabaseInstance(initDbConfig(dbOpts));

    DependencyProvider.getInstance()
      .register(new DatabaseDependency("account-db", dbOpts.host, dbOpts.port,
        userDb));

    return userDb;
  }

  /**
   * Initialize a connection wrapper to the user account database.
   *
   * @param opts Configuration options
   *
   * @return the initialized DatabaseInstance
   */
  public static DatabaseInstance initAccountDatabase(Options opts) {
    return getInstance().newAccountDatabase(opts);
  }

  /**
   * Initialize a connection wrapper to the user account database.
   *
   * @param opts Configuration options
   *
   * @return the initialized DatabaseInstance
   */
  public static DatabaseInstance initApplicationDatabase(Options opts) {
    return getInstance().newApplicationDatabase(opts);
  }

  /**
   * Initialize a connection wrapper to the user account database.
   *
   * @param opts Configuration options
   *
   * @return the initialized DatabaseInstance
   */
  public static DatabaseInstance initUserDatabase(Options opts) {
    return getInstance().newUserDatabase(opts);
  }

  /**
   * Gets the current account database connection or throws an exception if the
   * connection has not yet been established.
   */
  public DatabaseInstance getAccountDatabase() {
    if (Objects.isNull(acctDb))
      throw new IllegalStateException(String.format(ERR_NOT_INIT, "Account"));
    return acctDb;
  }

  /**
   * Gets the current account database connection or throws an exception if the
   * connection has not yet been established.
   */
  public static DatabaseInstance accountDatabase() {
    return getInstance().getAccountDatabase();
  }

  /**
   * Gets the current application database connection or throws an exception if
   * the connection has not yet been established.
   */
  public DatabaseInstance getApplicationDatabase() {
    if (Objects.isNull(appDb))
      throw new IllegalStateException(String.format(ERR_NOT_INIT, "Application"));
    return acctDb;
  }

  /**
   * Gets the current application database connection or throws an exception if
   * the connection has not yet been established.
   */
  public static DatabaseInstance applicationDatabase() {
    return getInstance().getApplicationDatabase();
  }

  /**
   * Gets the current user database connection or throws an exception if the
   * connection has not yet been established.
   */
  public DatabaseInstance getUserDatabase() {
    if (Objects.isNull(userDb))
      throw new IllegalStateException(String.format(ERR_NOT_INIT, "User"));
    return acctDb;
  }

  /**
   * Gets the current user database connection or throws an exception if the
   * connection has not yet been established.
   */
  public static DatabaseInstance userDatabase() {
    return getInstance().getUserDatabase();
  }

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Public Helpers                                                    ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  /**
   * Creates an Oracle JDBC connection string from the given options.
   */
  public String makeOracleJdbcUrl(DbOptions opts) {
    return String.format(ORACLE_URL, opts.host, opts.port, opts.name);
  }

  /**
   * Creates a Postgres JDBC connection string from the given options.
   */
  public String makePostgresJdbcUrl(DbOptions opts) {
    return String.format(POSTGRES_URL, opts.host, opts.port, opts.name);
  }


  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Internal Helpers                                                  ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  SimpleDbConfig initDbConfig(DbOptions opts) {
    return SimpleDbConfig.create(
      opts.platform,
      makeJdbcUrl(opts.platform, opts),
      opts.user,
      opts.pass,
      opts.threads);
  }

  String makeJdbcUrl(SupportedPlatform platform, DbOptions opts) {
    return switch (platform) {
      case ORACLE -> makeOracleJdbcUrl(opts);
      case POSTGRESQL -> makePostgresJdbcUrl(opts);
    };
  }
}
