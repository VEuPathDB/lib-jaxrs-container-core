package org.veupathdb.lib.container.jaxrs.server;

import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import jakarta.ws.rs.core.UriBuilder;

import java.util.Optional;

import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.health.Dependency;
import org.veupathdb.lib.container.jaxrs.providers.*;
import org.veupathdb.lib.container.jaxrs.utils.Cli;
import org.veupathdb.lib.container.jaxrs.utils.logging.Log;
import org.veupathdb.lib.container.jaxrs.utils.db.DbManager;
import org.veupathdb.lib.container.jaxrs.utils.ldap.OracleLDAPConfig;
import org.veupathdb.lib.container.jaxrs.utils.logging.LoggingVars;
import org.veupathdb.lib.prom.PrometheusJVM;

@SuppressWarnings("unused")
abstract public class Server
{
  private static final String
    ERR_MULTI_SERVER = "Cannot create more than one instance of Server",
    ERR_NO_SERVER    = "Called Server.getInstance() before a server was created.";

  private static final int DEFAULT_PORT = 8080;

  private static Server instance;
  private final  Logger logger;

  private ContainerResources resources;

  private boolean useAcctDb = false;
  private boolean useAppDb = false;
  private boolean useUserDb = false;

  private boolean checkUserQuerying = false;

  private HttpServer grizzly;

  public Server() {
    if (instance != null)
      throw new IllegalStateException(ERR_MULTI_SERVER);

    PrometheusJVM.enable();
    
    Log.initialize();
    this.logger = LogProvider.logger(Server.class);
    instance = this;
  }

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Override Points                                                   ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  /**
   * Creates a new Options object and returns it.
   *
   * @return newly created options object.
   */
  protected Options newOptions() {
    return new Options();
  }

  /**
   * Creates a new ContainerResources object which will be used to configure the
   * Grizzly {@link HttpServer}.
   *
   * @return newly created ContainerResources subclass.
   */
  protected abstract ContainerResources newResourceConfig(Options opts);

  /**
   * Extension point for registering external dependencies.
   *
   * @return An array of dependencies to register with the
   * {@link DependencyProvider}.
   */
  protected Dependency[] dependencies() {
    return new Dependency[0];
  }

  /**
   * Called on server shutdown to perform any tasks assigned by the overriding
   * subclass.
   */
  protected void onShutdown() {
  }

  /**
   * Hook-point for performing tasks after the CLI/Environment configuration has
   * been parsed.
   * <p>
   * This method will be called before the server is started and before any of
   * the built-in DB connections are established.
   */
  protected void postCliParse(Options opts) {
  }

  protected void postAcctDb() {}
  protected void postUserDb() {}
  protected void postAppDb() {}

  /**
   * Hook-point for performing tasks after the service has started up.
   */
  protected void postStartup() {}

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Service Settings                                                  ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  /**
   * Enables access to the account database.  If this is set, the ACCT_DB_* cli
   * options or environment variables must be set.
   *
   * @return Updated server instance.
   */
  protected final Server enableAccountDB() {
    useAcctDb = true;
    return this;
  }

  /**
   * Enables access to the application database.  If this is set, the ACCT_DB_*
   * cli options or environment variables must be set.
   *
   * @return Updated server instance.
   */
  protected final Server enableApplicationDB() {
    useAppDb = true;
    return this;
  }

  /**
   * Enables access to the user database.  If this is set, the ACCT_DB_* cli
   * options or environment variables must be set.
   *
   * @return Updated server instance.
   */
  protected final Server enableUserDB() {
    useUserDb = true;
    return this;
  }

  /**
   * Enables querying of users' data on the OAuth server by either user IDs or
   * by user emails.  If enabled, two additional configuration values are
   * required: oauth-client-id and oauth-client-secret
   *
   * @return Updated server instance.
   */
  protected final Server enableUserQuerying() {
    checkUserQuerying = true;
    return this;
  }

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Server Methods                                                    ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  public static Server getInstance() {
    if (instance == null)
      throw new IllegalStateException(ERR_NO_SERVER);
    return instance;
  }

  /**
   * Start up the HTTP server.
   *
   * @param cliArgs commandline arguments
   */
  protected final void start(String[] cliArgs) {
    logger.info("Starting server");
    OptionsProvider.setProvider(this::newOptions);

    var options = Cli.parseCLI(cliArgs, OptionsProvider.getOptions());
    postCliParse(options);

    // Initialize LDAP configuration.
    //
    // This must come before the DB initializations as they may depend on this
    // config.
    OracleLDAPConfig.initialize(options);

    if (useAcctDb) {
      logger.info("Account DB Enabled");
      DbManager.initAccountDatabase(options);
      postAcctDb();
    }

    if (useAppDb) {
      logger.info("Application DB Enabled");
      DbManager.initApplicationDatabase(options);
      postAppDb();
    }

    if (useUserDb) {
      logger.info("User DB Enabled");
      DbManager.initUserDatabase(options);
      postUserDb();
    }

    if (checkUserQuerying) {
      logger.info("User Querying Enabled");
      // this method will throw an illegal configuration exception if required env is not present
      OAuthProvider.getOAuthConfig();
    }

    for (var dep : dependencies())
      DependencyProvider.getInstance().register(dep);

    final var port = OptionsProvider.getOptions()
      .getServerPort()
      .orElse(DEFAULT_PORT);

    RuntimeProvider.runtime().addShutdownHook(new Thread(this::shutdown));

    try {
      grizzly = GrizzlyHttpServerFactory.createHttpServer(
        UriBuilder.fromUri("//0.0.0.0").port(port).build(),
        newResourceConfig(options)
      );
      grizzly.start();
    } catch (Throwable e) {
      logger.fatal("Could not start server.", e);
      RuntimeProvider.runtime().exit(1);
    }

    postStartup();

    logger.info("Server started.  Listening on port {}.", port);
  }

  protected final HttpServer getGrizzlyServer() {
    return grizzly;
  }

  private void shutdown() {
    LoggingVars.setNonRequestThreadVars("term0");
    logger.info("Server shutting down.");
    onShutdown();
    Optional.ofNullable(grizzly).ifPresent(HttpServer::shutdownNow);
    DependencyProvider.getInstance().shutDown();
  }
}
