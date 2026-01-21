package org.veupathdb.lib.container.jaxrs.utils.db;

import java.util.Objects;

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

  public DatabaseInstance newAccountDatabase(final Options opts) {
    if (Objects.nonNull(acctDb))
      return acctDb;

    return acctDb = newDatabase(opts.getAcctDbOpts());
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

  public static boolean hasAccountDatabase() {
    return getInstance().acctDb != null;
  }


  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Application Database                                              ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/


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

  public DatabaseInstance newApplicationDatabase(final Options opts) {
    if (Objects.nonNull(appDb))
      return appDb;

    return appDb = newDatabase(opts.getAppDbOpts());
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

  public static boolean hasApplicationDatabase() {
    return getInstance().appDb != null;
  }


  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    User Database                                                     ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

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

  public DatabaseInstance newUserDatabase(final Options opts) {
    if (Objects.nonNull(userDb))
      return userDb;

    return userDb = newDatabase(opts.getUserDbOpts());
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

  public static boolean hasUserDatabase() {
    return getInstance().userDb != null;
  }


  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Internal API                                                      ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/


  private DatabaseInstance newDatabase(final DbOptions opts) {
    var detail = new ConnectionDetailsImpl(opts);
    var db = new DatabaseInstance(detail.toFgpUtilConfig(), opts.displayName());

    var dependency = new FgpDatabaseDependency(opts.displayName(), detail.host(),
      detail.port(), db, db.getPlatform().getValidationQuery());

    DependencyProvider.getInstance()
      .register(dependency);

    DBPrometheus.register(opts.displayName(), db);

    return db;
  }
}
