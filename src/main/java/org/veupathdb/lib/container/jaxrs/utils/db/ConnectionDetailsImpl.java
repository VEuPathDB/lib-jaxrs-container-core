package org.veupathdb.lib.container.jaxrs.utils.db;

import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.db.pool.SimpleDbConfig;
import org.veupathdb.lib.container.jaxrs.config.DbOptions;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.utils.ldap.LDAPConfig;
import org.veupathdb.lib.ldap.LDAP;
import org.veupathdb.lib.ldap.LDAPHost;
import org.veupathdb.lib.ldap.NetDesc;

public class ConnectionDetailsImpl implements ConnectionDetails
{
  private static final String
    ERR_MISSING_ARGS = "Invalid DB connection config for %1$s.\n\n"
      + "See the project readme for information about how to configure/run this"
      + " service.";

  private final NetDesc connectionInfo;
  private final SupportedPlatform platform;
  private final String connectionUrl;
  private final String user;
  private final String pass;
  private final int poolSize;
  private final int defaultFetchSize;

  /**
   * Creates a {@link ConnectionDetails} instance from the input options.
   */
  public ConnectionDetailsImpl(final DbOptions opts) {
    final var log = LogProvider.logger(ConnectionDetails.class);
    log.debug("Setting up connection for db {}", opts.displayName());

    if (opts.lookupCn().isPresent()) {
      final var hosts = LDAPConfig.getInstance().hosts().orElseThrow(missingLdapInfo(opts, "hosts"))
          .stream().map(LDAPHost::ofString).collect(Collectors.toList());
      final var baseDn = LDAPConfig.getInstance().baseDn().orElseThrow(missingLdapInfo(opts, "baseDn"));

      connectionInfo = new LDAP(new org.veupathdb.lib.ldap.LDAPConfig(hosts, baseDn)).requireSingularNetDesc(opts.lookupCn().get());
      platform = SupportedPlatform.fromLdapPlatform(connectionInfo.getPlatform());
      connectionUrl = platform.getPlatformInstance().getConnectionUrl(
          connectionInfo.getHost(), connectionInfo.getPort(), connectionInfo.getIdentifier());
    }
    else {
      platform = opts.platform().orElseThrow(missingPropErr(opts));
      var host = opts.host().orElseThrow(missingPropErr(opts));
      var port = opts.port().orElse(platform.getDefaultPort());
      var name = opts.name().orElseThrow(missingPropErr(opts));
      connectionUrl = platform.getPlatformInstance().getConnectionUrl(host, port, name);
      connectionInfo = DBPlatform.parseConnectionUrl(connectionUrl).getLeft();
    }

    user = opts.user().orElseThrow(missingPropErr(opts));
    pass = opts.pass().orElseThrow(missingPropErr(opts));
    poolSize = opts.poolSize().orElse(DbManager.DEFAULT_POOL_SIZE);
    defaultFetchSize = opts.defaultFetchSize().orElse(DbManager.DEFAULT_FETCH_SIZE);
  }

  private static Supplier<RuntimeException> missingLdapInfo(DbOptions opts, String field) {
    return () -> new RuntimeException(opts.displayName() +
        " was configured with lookupCn but LDAP " + field + " value was not configured.");
  }

  protected ConnectionDetailsImpl(
    NetDesc connectionInfo,
    String user,
    String pass,
    int poolSize,
    int defaultFetchSize
  ) {
    this.connectionInfo = connectionInfo;
    this.platform = SupportedPlatform.fromLdapPlatform(connectionInfo.getPlatform());
    this.connectionUrl = platform.getPlatformInstance().getConnectionUrl(
        connectionInfo.getHost(), connectionInfo.getPort(), connectionInfo.getIdentifier());
    this.user = user;
    this.pass = pass;
    this.poolSize = poolSize;
    this.defaultFetchSize = defaultFetchSize;
  }

  @Override
  public String host() {
    return connectionInfo.getHost();
  }

  @Override
  public int port() {
    return connectionInfo.getPort();
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
    return connectionInfo.getIdentifier();
  }

  @Override
  public String toJdbcString() {
    return connectionUrl;
  }

  @Override
  public int poolSize() {
    return poolSize;
  }

  @Override
  public ConnectionPoolConfig toFgpUtilConfig() {
    return SimpleDbConfig.create(
        platform,
        connectionUrl,
        user,
        pass,
        poolSize,
      defaultFetchSize
    );
  }

  private static Supplier <RuntimeException> missingPropErr(final DbOptions opts) {
    return () -> new RuntimeException(String.format(ERR_MISSING_ARGS, opts.displayName()));
  }
}
