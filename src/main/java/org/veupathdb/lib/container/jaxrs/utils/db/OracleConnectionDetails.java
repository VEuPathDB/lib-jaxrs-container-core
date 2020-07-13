package org.veupathdb.lib.container.jaxrs.utils.db;

import java.util.regex.Pattern;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.db.pool.SimpleDbConfig;
import org.veupathdb.lib.container.jaxrs.config.DbOptions;
import org.veupathdb.lib.container.jaxrs.utils.Patterns;
import org.veupathdb.lib.container.jaxrs.utils.ldap.LDAP;
import org.veupathdb.lib.container.jaxrs.utils.ldap.OracleLDAPConfig;

public class OracleConnectionDetails extends RawConnectionDetails
{
  private static final String
    LDAP_JDBC_PATTERN = "jdbc:oracle:thin:@%s",
    LDAP_JDBC_SEGMENT = "ldap://%s/%s",
    LDAP_JDBC_JOIN    = " ",
    RAW_JDBC_PATTERN  = "jdbc:oracle:oci:@//%s:%d/%s";

  private static final Pattern
    PAT_HOST = Pattern.compile("\\(HOST=([^)]+)\\)"),
    PAT_PORT = Pattern.compile("\\(PORT=(\\d+)\\)"),
    PAT_NAME = Pattern.compile("\\(SERVICE_NAME=([^)]+)\\)");

  private static final SupportedPlatform platform = SupportedPlatform.ORACLE;

  private static final int DEFAULT_PORT = 1521;

  private final String tsName;

  private OracleConnectionDetails(
    final String tsName,
    final int poolSize
  ) {
    this.tsName = tsName;

    final var rawValue = new LDAP().requireOracleDetails(tsName);

    host(Patterns.firstGroup(PAT_HOST, rawValue).orElseThrow());
    port(Integer.parseInt(Patterns.firstGroup(PAT_PORT, rawValue).orElseThrow()));
    dbName(Patterns.firstGroup(PAT_NAME, rawValue).orElseThrow());
    poolSize(poolSize);
  }

  private OracleConnectionDetails(
    final String host,
    final int port,
    final String name,
    final String user,
    final String pass,
    final int poolSize,
    final String tsName
  ) {
    super(host, port, name, user, pass, poolSize);
    this.tsName = tsName;
  }

  public String tsName() {
    return tsName;
  }

  @Override
  public String toJdbcString() {
    return tsName != null
      ? toLdapJdbcString()
      : String.format(RAW_JDBC_PATTERN, host(), port(), dbName());
  }

  @Override
  public ConnectionPoolConfig toFgpUtilConfig() {
    return SimpleDbConfig.create(
      platform,
      toJdbcString(),
      user(),
      password(),
      poolSize()
    );
  }

  public static OracleConnectionDetails fromOptions(final DbOptions opts) {
    return opts.tnsName().isPresent()
      ? fromLdap(opts)
      : fromRaw(opts);
  }

  private String toLdapJdbcString() {
    final var hosts = OracleLDAPConfig.getInstance().hosts();
    final var base  = OracleLDAPConfig.getInstance().baseDn();

    final var fill = new String[hosts.length];
    final var ctx  = String.format(LDAP.ORA_CTX_PATTERN, tsName,
      LDAP.ORA_CONTEXT, base);

    for (var i = 0; i < fill.length; i++)
      fill[i] = String.format(LDAP_JDBC_SEGMENT, hosts[i], ctx);


    return String.format(LDAP_JDBC_PATTERN, String.join(LDAP_JDBC_JOIN, fill));
  }

  private static OracleConnectionDetails fromLdap(final DbOptions opts) {
    return new OracleConnectionDetails(
      opts.tnsName().orElseThrow(),
      opts.poolSize().orElse(DbManager.DEFAULT_POOL_SIZE)
    );
  }

  private static OracleConnectionDetails fromRaw(final DbOptions opts) {
    return new OracleConnectionDetails(
      opts.host().orElseThrow(missingPropErr(opts)),
      opts.port().orElse(DEFAULT_PORT),
      opts.name().orElseThrow(missingPropErr(opts)),
      opts.user().orElseThrow(missingPropErr(opts)),
      opts.pass().orElseThrow(missingPropErr(opts)),
      opts.poolSize().orElse(DbManager.DEFAULT_POOL_SIZE),
      null
    );
  }
}
