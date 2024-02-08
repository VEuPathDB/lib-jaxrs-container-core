package org.veupathdb.lib.container.jaxrs.config;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import picocli.CommandLine.Option;

import java.util.Optional;

/**
 * CLI Options.
 */
@SuppressWarnings("unused")
public class Options {

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Configuration Properties                                          ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃    General Config                                  ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  @Option(
    names = "--admin-auth-token",
    defaultValue = "${env:ADMIN_AUTH_TOKEN}",
    description = "env: ADMIN_AUTH_TOKEN",
    arity = "1")
  private String adminAuthToken;

  @Option(
    names = "--server-port",
    defaultValue = "${env:SERVER_PORT}",
    description = "env: SERVER_PORT",
    arity = "1")
  private Integer serverPort;

  @Option(
    names = "--enable-cors",
    defaultValue = "${env:ENABLE_CORS}",
    description = "env: ENABLE_CORS",
    arity = "1")
  private Boolean enableCors;

  @Option(
    names = "--ldap-server",
    defaultValue = "${env:LDAP_SERVER}",
    description = "env: LDAP_SERVER\nFormatted as <ldap.host.name>:<port>",
    arity = "1..*")
  private String ldapServers;

  @Option(
    names = "--oracle-base-dn",
    defaultValue = "${env:ORACLE_BASE_DN}",
    description = "env: ORACLE_BASE_DN",
    arity = "1")
  private String oracleBaseDn;

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃    Authentication/OAuth Config                     ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  @Option(
      names = "--auth-secret",
      defaultValue = "${env:AUTH_SECRET_KEY}",
      description = "env: AUTH_SECRET_KEY",
      arity = "1")
  private String authSecretKey;

  @Option(
      names = "--oauth-url",
      defaultValue = "${env:OAUTH_URL}",
      description = "env: OAUTH_URL",
      arity = "1")
  private String oauthUrl;

  @Option(
      names = "--oauth-client-id",
      defaultValue = "${env:OAUTH_CLIENT_ID}",
      description = "env: OAUTH_CLIENT_ID",
      arity = "1")
  private String oauthClientId;

  @Option(
      names = "--oauth-client-secret",
      defaultValue = "${env:OAUTH_CLIENT_SECRET}",
      description = "env: OAUTH_CLIENT_SECRET",
      arity = "1")
  private String oauthClientSecret;

  // Do we need a mount to /etc/pki/java/cacerts ???
  @Option(
      names = "--key-store-file",
      defaultValue = "${env:KEY_STORE_FILE}",
      description = "env: KEY_STORE_FILE",
      arity = "1")
  private String keyStoreFile;

  @Option(
      names = "--key-store-pass-phrase",
      defaultValue = "${env:KEY_STORE_PASS_PHRASE}",
      description = "env: KEY_STORE_PASS_PHRASE",
      arity = "1")
  private String keyStorePassPhrase;

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃    Application DB Config                           ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  @Option(
    names = "--app-db-ora",
    defaultValue = "${env:APP_DB_TNS_NAME}",
    description = "env: APP_DB_TNS_NAME",
    arity = "1")
  private String appDbTsName;

  @Option(
    names = "--app-db-host",
    defaultValue = "${env:APP_DB_HOST}",
    description = "env: APP_DB_HOST",
    arity = "1")
  private String appDbHost;

  @Option(
    names = "--app-db-port",
    defaultValue = "${env:APP_DB_PORT}",
    description = "env: APP_DB_PORT\nDefaults to database platform default.",
    arity = "1")
  private Integer appDbPort;

  @Option(
    names = "--app-db-user",
    defaultValue = "${env:APP_DB_USER}",
    description = "env: APP_DB_USER",
    arity = "1")
  private String appDbUser;

  @Option(
    names = "--app-db-name",
    defaultValue = "${env:APP_DB_NAME}",
    description = "env: APP_DB_NAME", arity = "1")
  private String appDbName;

  @Option(
    names = "--app-db-pass",
    defaultValue = "${env:APP_DB_PASS}",
    description = "env: APP_DB_PASS",
    arity = "1")
  private String appDbPass;

  @Option(
    names = "--app-db-pool-size",
    defaultValue = "${env:APP_DB_POOL_SIZE}",
    description = "env: APP_DB_POOL_SIZE\nDefaults to 20",
    arity = "1")
  private Integer appDbPoolSize;

  @Option(
    names = "--app-db-platform",
    defaultValue = "${env:APP_DB_PLATFORM}",
    description = "env: APP_DB_PLATFORM\nDefaults to Oracle.",
    arity = "1")
  private SupportedPlatform appDbPlatform;

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃    Account DB Config                               ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  @Option(
    names = "--acct-db-ora",
    defaultValue = "${env:ACCT_DB_TNS_NAME}",
    description = "env: ACCT_DB_TNS_NAME",
    arity = "1")
  private String acctDbTsName;

  @Option(
    names = "--acct-db-host",
    defaultValue = "${env:ACCT_DB_HOST}",
    description = "env: ACCT_DB_HOST",
    arity = "1")
  private String acctDbHost;

  @Option(
    names = "--acct-db-port",
    defaultValue = "${env:ACCT_DB_PORT}",
    description = "env: ACCT_DB_PORT\nDefaults to database platform default.",
    arity = "1")
  private Integer acctDbPort;

  @Option(
    names = "--acct-db-user",
    defaultValue = "${env:ACCT_DB_USER}",
    description = "env: ACCT_DB_USER",
    arity = "1")
  private String acctDbUser;

  @Option(
    names = "--acct-db-name",
    defaultValue = "${env:ACCT_DB_NAME}",
    description = "env: ACCT_DB_NAME", arity = "1")
  private String acctDbName;

  @Option(
    names = "--acct-db-pass",
    defaultValue = "${env:ACCT_DB_PASS}",
    description = "env: ACCT_DB_PASS",
    arity = "1")
  private String acctDbPass;

  @Option(
    names = "--acct-db-pool-size",
    defaultValue = "${env:ACCT_DB_POOL_SIZE}",
    description = "env: ACCT_DB_POOL_SIZE\nDefaults to 20",
    arity = "1")
  private Integer acctDbPoolSize;

  @Option(
    names = "--acct-db-platform",
    defaultValue = "${env:ACCT_DB_PLATFORM}",
    description = "env: ACCT_DB_PLATFORM\nDefaults to Oracle.",
    arity = "1")
  private SupportedPlatform acctDbPlatform;

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃    User DB Config                                  ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  @Option(
    names = "--user-db-ora",
    defaultValue = "${env:USER_DB_TNS_NAME}",
    description = "env: USER_DB_TNS_NAME",
    arity = "1")
  private String userDbTsName;

  @Option(
    names = "--user-db-host",
    defaultValue = "${env:USER_DB_HOST}",
    description = "env: USER_DB_HOST",
    arity = "1")
  private String userDbHost;

  @Option(
    names = "--user-db-port",
    defaultValue = "${env:USER_DB_PORT}",
    description = "env: USER_DB_PORT\nDefaults to database platform default.",
    arity = "1")
  private Integer userDbPort;

  @Option(
    names = "--user-db-user",
    defaultValue = "${env:USER_DB_USER}",
    description = "env: USER_DB_USER",
    arity = "1")
  private String userDbUser;

  @Option(
    names = "--user-db-name",
    defaultValue = "${env:USER_DB_NAME}",
    description = "env: USER_DB_NAME", arity = "1")
  private String userDbName;

  @Option(
    names = "--user-db-pass",
    defaultValue = "${env:USER_DB_PASS}",
    description = "env: USER_DB_PASS",
    arity = "1")
  private String userDbPass;

  @Option(
    names = "--user-db-pool-size",
    defaultValue = "${env:USER_DB_POOL_SIZE}",
    description = "env: USER_DB_POOL_SIZE\nDefaults to 20",
    arity = "1")
  private Integer userDbPoolSize;

  @Option(
    names = "--user-db-platform",
    defaultValue = "${env:USER_DB_PLATFORM}",
    description = "env: USER_DB_PLATFORM\nDefaults to Oracle.",
    arity = "1")
  private SupportedPlatform userDbPlatform;

  @Option(
    names = "--user-db-schema",
    defaultValue = "${env:USER_DB_SCHEMA}",
    description = "env: USER_DB_SCHEMA\nDefaults to userlogins5",
    arity = "1")
  private String userDbSchema;

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Property Getters                                                  ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  public Optional<String> getAdminAuthToken() {
    return Optional.ofNullable(adminAuthToken);
  }

  public Optional<Integer> getServerPort() {
    return Optional.ofNullable(serverPort);
  }

  public boolean getCorsEnabled() {
    return enableCors != null && enableCors;
  }

  public Optional<String> getAuthSecretKey() {
    return Optional.ofNullable(authSecretKey);
  }

  public Optional<String> getOAuthUrl() {
    return Optional.ofNullable(oauthUrl);
  }

  public Optional<String> getOAuthClientId() {
    return Optional.ofNullable(oauthClientId);
  }

  public Optional<String> getOAuthClientSecret() {
    return Optional.ofNullable(oauthClientSecret);
  }

  public Optional<String> getKeyStoreFile() {
    return Optional.ofNullable(keyStoreFile);
  }

  public Optional<String> getKeyStorePassPhrase() {
    return Optional.ofNullable(keyStorePassPhrase);
  }

  public DbOptions getAppDbOpts() {
    return new DbOptionsImpl(appDbTsName, appDbHost, appDbPort, appDbName,
      appDbUser, appDbPass, appDbPlatform, appDbPoolSize, "app-db"
    );
  }

  public DbOptions getAcctDbOpts() {
    return new DbOptionsImpl(acctDbTsName, acctDbHost, acctDbPort, acctDbName,
      acctDbUser, acctDbPass, acctDbPlatform, acctDbPoolSize, "acct-db"
    );
  }

  public DbOptions getUserDbOpts() {
    return new DbOptionsImpl(userDbTsName, userDbHost, userDbPort, userDbName,
      userDbUser, userDbPass, userDbPlatform, userDbPoolSize, "user-db"
    );
  }

  /**
   * Retrieves either the configured user schema or the default value
   * {@code userlogins5} if none was configured.
   *
   * @return The configured or default user schema name.
   */
  public String getUserDbSchema() {
    return userDbSchema == null || userDbSchema.isBlank()
      ? "userlogins5"
      : userDbSchema;
  }

  public Optional<String> getLdapServers() {
    return Optional.ofNullable(ldapServers);
  }

  public Optional<String> getOracleBaseDn() {
    return Optional.ofNullable(oracleBaseDn);
  }
}
