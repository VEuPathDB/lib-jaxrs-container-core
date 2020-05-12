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

  @Option(names = "--auth-secret", defaultValue = "${env:AUTH_SECRET_KEY}", description = "env: AUTH_SECRET_KEY", arity = "1")
  private String authSecretKey;

  @Option(names = "--server-port", defaultValue = "${env:SERVER_PORT}", description = "env: SERVER_PORT", arity = "1")
  private Integer serverPort;

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃    Application DB Config                           ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

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


  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃                                                                      ┃
    ┃    Property Getters                                                  ┃
    ┃                                                                      ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  public Optional<String> getAuthSecretKey() {
    return Optional.ofNullable(authSecretKey);
  }

  public Optional<Integer> getServerPort() {
    return Optional.ofNullable(serverPort);
  }

  public Optional<String> getAppDbHost() {
    return Optional.ofNullable(appDbHost);
  }

  public Optional<Integer> getAppDbPort() {
    return Optional.ofNullable(appDbPort);
  }

  public Optional<String> getAppDbUser() {
    return Optional.ofNullable(appDbUser);
  }

  public Optional<String> getAppDbName() {
    return Optional.ofNullable(appDbName);
  }

  public Optional<String> getAppDbPass() {
    return Optional.ofNullable(appDbPass);
  }

  public Optional<Integer> getAppDbPoolSize() {
    return Optional.ofNullable(appDbPoolSize);
  }

  public Optional<SupportedPlatform> getAppDbPlatform() {
    return Optional.ofNullable(appDbPlatform);
  }

  public Optional<String> getAcctDbHost() {
    return Optional.ofNullable(acctDbHost);
  }

  public Optional<Integer> getAcctDbPort() {
    return Optional.ofNullable(acctDbPort);
  }

  public Optional<String> getAcctDbUser() {
    return Optional.ofNullable(acctDbUser);
  }

  public Optional<String> getAcctDbName() {
    return Optional.ofNullable(acctDbName);
  }

  public Optional<String> getAcctDbPass() {
    return Optional.ofNullable(acctDbPass);
  }

  public Optional<Integer> getAcctDbPoolSize() {
    return Optional.ofNullable(acctDbPoolSize);
  }

  public Optional<SupportedPlatform> getAcctDbPlatform() {
    return Optional.ofNullable(acctDbPlatform);
  }

  public Optional<String> getUserDbHost() {
    return Optional.ofNullable(userDbHost);
  }

  public Optional<Integer> getUserDbPort() {
    return Optional.ofNullable(userDbPort);
  }

  public Optional<String> getUserDbUser() {
    return Optional.ofNullable(userDbUser);
  }

  public Optional<String> getUserDbName() {
    return Optional.ofNullable(userDbName);
  }

  public Optional<String> getUserDbPass() {
    return Optional.ofNullable(userDbPass);
  }

  public Optional<Integer> getUserDbPoolSize() {
    return Optional.ofNullable(userDbPoolSize);
  }

  public Optional<SupportedPlatform> getUserDbPlatform() {
    return Optional.ofNullable(userDbPlatform);
  }
}
