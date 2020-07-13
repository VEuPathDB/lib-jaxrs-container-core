package org.veupathdb.lib.container.jaxrs.utils.db;

import org.veupathdb.lib.container.jaxrs.config.DbOptions;

abstract class RawConnectionDetails implements ConnectionDetails
{
  private static final String
    ERR_MISSING_ARGS = "Invalid DB connection config for %1$s.\n\n"
      + "See the project readme for information about how to configure/run this"
      + " service.";

  private String host;
  private int    port;
  private String dbName;
  private String user;
  private String pass;
  private int    poolSize;

  protected RawConnectionDetails() {}

  protected RawConnectionDetails(
    String host,
    int port,
    String dbName,
    String user,
    String pass,
    int poolSize
  ) {
    this.host = host;
    this.port = port;
    this.dbName = dbName;
    this.user = user;
    this.pass = pass;
    this.poolSize = poolSize;
  }

  @Override
  public String host() {
    return host;
  }

  @Override
  public int port() {
    return port;
  }

  @Override
  public String user() {
    return user;
  }

  @Override
  public String password() {
    return pass;
  }

  @Override
  public String dbName() {
    return dbName;
  }

  @Override
  public int poolSize() {
    return poolSize;
  }

  protected RawConnectionDetails host(final String host) {
    this.host = host;
    return this;
  }

  protected RawConnectionDetails port(final int port) {
    this.port = port;
    return this;
  }

  protected RawConnectionDetails user(final String user) {
    this.user = user;
    return this;
  }

  protected RawConnectionDetails pass(final String pass) {
    this.pass = pass;
    return this;
  }

  protected RawConnectionDetails dbName(final String dbName) {
    this.dbName = dbName;
    return this;
  }

  protected RawConnectionDetails poolSize(final int poolSize) {
    this.poolSize = poolSize;
    return this;
  }

  protected static RuntimeException missingPropErr(final DbOptions opts) {
    return new RuntimeException(String.format(ERR_MISSING_ARGS,
      opts.displayName()));
  }
}
