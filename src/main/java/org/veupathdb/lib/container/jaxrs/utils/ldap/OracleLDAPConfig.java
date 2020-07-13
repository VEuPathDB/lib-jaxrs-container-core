package org.veupathdb.lib.container.jaxrs.utils.ldap;

import java.util.Objects;

import org.veupathdb.lib.container.jaxrs.config.Options;

public class OracleLDAPConfig
{
  private static final String
    ERR_NO_HOST = "Attempted to use LDAP settings with no hosts configured.",
    ERR_NO_BASE = "Attempted to use LDAP settings with no base DN configured.";

  private static OracleLDAPConfig instance;

  private String[] hosts;
  private String   baseDn;

  public String[] hosts() {
    if (hosts == null)
      throw new RuntimeException(ERR_NO_HOST);

    return hosts;
  }

  public String baseDn() {
    if (baseDn == null)
      throw new RuntimeException(ERR_NO_BASE);

    return baseDn;
  }

  public static void initialize(final Options opts) {
    final var instance = new OracleLDAPConfig();

    instance.hosts = opts.getLdapServers().orElse("").split(",");
    instance.baseDn = opts.getOracleBaseDn().orElse(null);

    OracleLDAPConfig.instance = instance;
  }

  public static OracleLDAPConfig getInstance() {
    return Objects.requireNonNull(instance);
  }
}
