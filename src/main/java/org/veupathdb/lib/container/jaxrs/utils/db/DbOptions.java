package org.veupathdb.lib.container.jaxrs.utils.db;

import org.gusdb.fgputil.db.platform.SupportedPlatform;

import java.util.function.Supplier;

import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;

import static org.gusdb.fgputil.db.platform.SupportedPlatform.ORACLE;

final class DbOptions {

  private static final String
    ERR_NO_HOST = "No configured %s database host",
    ERR_NO_NAME = "No configured %s database name",
    ERR_NO_USER = "No configured %s database username",
    ERR_NO_PASS = "No configured %s database password";

  String host;

  String name;

  int port;

  String user;

  String pass;

  SupportedPlatform platform;

  int threads;

  DbOptions() {}

  private DbOptions(
    String host,
    String name,
    int port,
    String user,
    String pass,
    SupportedPlatform platform,
    int threads
  ) {
    this.host = host;
    this.port = port;
    this.name = name;
    this.user = user;
    this.pass = pass;
    this.platform = platform;
    this.threads = threads;
  }

  static DbOptions forAccountDB(Options o) {
    var plat = o.getAcctDbPlatform().orElse(DbManager.DEFAULT_PLATFORM);
    var db   = "account";

    return new DbOptions(
      o.getAcctDbHost().orElseThrow(confErr(ERR_NO_HOST, db)),
      o.getAcctDbName().orElseThrow(confErr(ERR_NO_NAME, db)),
      o.getAcctDbPort().orElse(plat == ORACLE ? DbManager.ORACLE_PORT : DbManager.POSTGRES_PORT),
      o.getAcctDbUser().orElseThrow(confErr(ERR_NO_USER, db)),
      o.getAcctDbPass().orElseThrow(confErr(ERR_NO_PASS, db)),
      o.getAcctDbPlatform().orElse(DbManager.DEFAULT_PLATFORM),
      o.getAcctDbPoolSize().orElse(DbManager.DEFAULT_POOL_SIZE));
  }

  static DbOptions forAppDB(Options o) {
    var plat = o.getAppDbPlatform().orElse(DbManager.DEFAULT_PLATFORM);
    var db   = "application";

    return new DbOptions(
      o.getAppDbHost().orElseThrow(confErr(ERR_NO_HOST, db)),
      o.getAppDbName().orElseThrow(confErr(ERR_NO_NAME, db)),
      o.getAppDbPort().orElse(plat == ORACLE ? DbManager.ORACLE_PORT : DbManager.POSTGRES_PORT),
      o.getAppDbUser().orElseThrow(confErr(ERR_NO_USER, db)),
      o.getAppDbPass().orElseThrow(confErr(ERR_NO_PASS, db)),
      o.getAppDbPlatform().orElse(DbManager.DEFAULT_PLATFORM),
      o.getAppDbPoolSize().orElse(DbManager.DEFAULT_POOL_SIZE));
  }

  static DbOptions forUserDB(Options o) {
    var plat = o.getUserDbPlatform().orElse(DbManager.DEFAULT_PLATFORM);
    var db   = "application";

    return new DbOptions(
      o.getUserDbHost().orElseThrow(confErr(ERR_NO_HOST, db)),
      o.getUserDbName().orElseThrow(confErr(ERR_NO_NAME, db)),
      o.getUserDbPort().orElse(plat == ORACLE ? DbManager.ORACLE_PORT : DbManager.POSTGRES_PORT),
      o.getUserDbUser().orElseThrow(confErr(ERR_NO_USER, db)),
      o.getUserDbPass().orElseThrow(confErr(ERR_NO_PASS, db)),
      o.getUserDbPlatform().orElse(DbManager.DEFAULT_PLATFORM),
      o.getUserDbPoolSize().orElse(DbManager.DEFAULT_POOL_SIZE));
  }

  static Supplier<InvalidConfigException> confErr(String message, String db) {
    return () -> new InvalidConfigException(String.format(message, db));
  }
}
