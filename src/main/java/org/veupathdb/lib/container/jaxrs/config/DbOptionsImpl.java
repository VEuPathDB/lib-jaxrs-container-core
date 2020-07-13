package org.veupathdb.lib.container.jaxrs.config;

import java.util.Optional;

import org.gusdb.fgputil.db.platform.SupportedPlatform;

public class DbOptionsImpl implements DbOptions
{
  private final String tnsName;
  private final String host;
  private final Integer           port;
  private final String            name;
  private final String            user;
  private final String            pass;
  private final SupportedPlatform platform;
  private final Integer           poolSize;
  private final String            displayName;

  public DbOptionsImpl(
    String tnsName,
    String host,
    Integer port,
    String name,
    String user,
    String pass,
    SupportedPlatform platform,
    Integer poolSize,
    String displayName
  ) {
    this.tnsName = tnsName;
    this.host = host;
    this.port = port;
    this.name = name;
    this.user = user;
    this.pass = pass;
    this.platform = platform;
    this.poolSize = poolSize;
    this.displayName = displayName;
  }

  @Override
  public Optional < String > tnsName() {
    return Optional.ofNullable(tnsName);
  }

  @Override
  public Optional < String > host() {
    return Optional.ofNullable(host);
  }

  @Override
  public Optional < Integer > port() {
    return Optional.ofNullable(port);
  }

  @Override
  public Optional < String > name() {
    return Optional.ofNullable(name);
  }

  @Override
  public Optional < String > user() {
    return Optional.ofNullable(user);
  }

  @Override
  public Optional < String > pass() {
    return Optional.ofNullable(pass);
  }

  @Override
  public Optional < SupportedPlatform > platform() {
    return Optional.ofNullable(platform);
  }

  @Override
  public Optional < Integer > poolSize() {
    return Optional.ofNullable(poolSize);
  }

  @Override
  public String displayName() {
    return displayName;
  }
}
