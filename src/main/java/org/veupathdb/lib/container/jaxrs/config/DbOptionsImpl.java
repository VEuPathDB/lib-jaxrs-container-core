package org.veupathdb.lib.container.jaxrs.config;

import java.util.Optional;

import org.gusdb.fgputil.db.platform.SupportedPlatform;

public class DbOptionsImpl implements DbOptions {
  private final String            lookupCn;
  private final String            host;
  private final Integer           port;
  private final String            name;
  private final String            user;
  private final String            pass;
  private final SupportedPlatform platform;
  private final Integer           poolSize;
  private final String            displayName;
  private final Integer           defaultFetchSize;

  public DbOptionsImpl(
    String lookupCn,
    String host,
    Integer port,
    String name,
    String user,
    String pass,
    SupportedPlatform platform,
    Integer poolSize,
    String displayName,
    Integer defaultFetchSize
  ) {
    this.lookupCn    = lookupCn;
    this.host        = host;
    this.port        = port;
    this.name        = name;
    this.user        = user;
    this.pass        = pass;
    this.platform    = platform;
    this.poolSize    = poolSize;
    this.displayName = displayName;
    this.defaultFetchSize = defaultFetchSize;
  }

  @Override
  public Optional<String> lookupCn() {
    return Optional.ofNullable(lookupCn);
  }

  @Override
  public Optional<String> host() {
    return Optional.ofNullable(host);
  }

  @Override
  public Optional<Integer> port() {
    return Optional.ofNullable(port);
  }

  @Override
  public Optional<String> name() {
    return Optional.ofNullable(name);
  }

  @Override
  public Optional<String> user() {
    return Optional.ofNullable(user);
  }

  @Override
  public Optional<String> pass() {
    return Optional.ofNullable(pass);
  }

  @Override
  public Optional<SupportedPlatform> platform() {
    return Optional.ofNullable(platform);
  }

  @Override
  public Optional<Integer> poolSize() {
    return Optional.ofNullable(poolSize);
  }

  @Override
  public Optional<Integer> defaultFetchSize() {
    return Optional.ofNullable(defaultFetchSize);
  }

  @Override
  public String displayName() {
    return displayName;
  }
}
