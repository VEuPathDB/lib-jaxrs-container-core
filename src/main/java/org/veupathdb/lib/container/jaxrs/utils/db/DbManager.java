package org.veupathdb.lib.container.jaxrs.utils.db;

import java.util.Objects;

import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.veupathdb.lib.container.jaxrs.config.DbOptions;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.health.FgpDatabaseDependency;
import org.veupathdb.lib.container.jaxrs.providers.DependencyProvider;

/**
 * Database Manager.
 * <p>
 * Handles the connection to DatabaseInstances and provides singleton access to
 * them if needed.
 */
public class DbManager
{
  private DbManager() {
  }

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
    ┃    General Constants                                                 ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

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

  public DatabaseInstance newAccountDatabase(final Options opts) {
    if (Objects.nonNull(acctDb))
      return acctDb;

    return acctDb = newDatabase(opts.getAcctDbOpts());
  }

  public DatabaseInstance newApplicationDatabase(final Options opts) {
    if (Objects.nonNull(appDb))
      return appDb;

    return appDb = newDatabase(opts.getAppDbOpts());
  }

  public DatabaseInstance newUserDatabase(final Options opts) {
    if (Objects.nonNull(userDb))
      return userDb;

    return userDb = newDatabase(opts.getUserDbOpts());
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
      throw new IllegalStateException(String.format(
        ERR_NOT_INIT,
        "Application"
      ));
    return appDb;
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
    return userDb;
  }

  /**
   * Gets the current user database connection or throws an exception if the
   * connection has not yet been established.
   */
  public static DatabaseInstance userDatabase() {
    return getInstance().getUserDatabase();
  }

  private DatabaseInstance newDatabase(final DbOptions opts) {
    var detail = ConnectionDetails.fromOptions(opts);
    var db = new DatabaseInstance(detail.toFgpUtilConfig(), opts.displayName());

    DependencyProvider.getInstance()
      .register(new FgpDatabaseDependency(opts.displayName(), detail.host(),
        detail.port(), db));

    return db;
  }
}
