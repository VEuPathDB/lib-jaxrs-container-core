package org.veupathdb.lib.container.jaxrs.utils.ldap;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPSearchException;
import com.unboundid.ldap.sdk.SearchScope;
import org.apache.logging.log4j.Logger;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

public class LDAP
{
  public static final String
    ORA_FILTER = "objectclass=*",
    ORA_ATTR = "orclNetDescString",
    ORA_CTX_PATTERN = "cn=%s,%s,%s",
    ORA_CONTEXT = "cn=OracleContext";

  /**
   * Error Messages
   */
  private static final String
    ERR_CONNECT_FAILED = "Failed to connect to the configured LDAP server.",
    ERR_LOOKUP_FAILED  = "Failed to search for LDAP entry",
    ERR_LOOKUP_NONE    = "Failed to locate LDAP entry for tnsName %s",
    ERR_LOOKUP_MULTI   = "Multiple LDAP entries match tnsName %s",
    ERR_NO_ATTRIBUTE   = "LDAP entry is missing required attribute " + ORA_ATTR;

  private final Logger log = LogProvider.logger(LDAP.class);

  private LDAPConnection connection;

  public String requireOracleDetails(final String tsName) {
    log.trace("LDAP#requireOracleDetails({})", tsName);

    try (final var con = getConnection()) {
      final var query = String.format(ORA_CTX_PATTERN, tsName, ORA_CONTEXT,
        OracleLDAPConfig.getInstance().baseDn()
      );

      log.debug("Running LDAP query for \"{}\"", query);
      final var res = con.search(query, SearchScope.BASE, ORA_FILTER);

      if (res.getEntryCount() == 0)
        throw new RuntimeException(String.format(ERR_LOOKUP_NONE, tsName));

      if (res.getEntryCount() > 1)
        throw new RuntimeException(String.format(ERR_LOOKUP_MULTI, tsName));

      final var attr = res.getSearchEntries().get(0).getAttribute(ORA_ATTR);

      if (attr == null)
        throw new RuntimeException(ERR_NO_ATTRIBUTE);

      return attr.getValue();
    } catch (LDAPSearchException e) {
      throw new RuntimeException(ERR_LOOKUP_FAILED, e);
    }
  }

  private LDAPConnection getConnection() {
    log.trace("LDAP#getConnection()");
    if (connection == null) {
      final var conf = OracleLDAPConfig.getInstance();

      for (int i = 0; i < conf.hosts().length; i++) {
        log.debug("Attempting to connect to LDAP host #" + (i + 1));
        var split = conf.hosts()[i].split(":");

        try {
          connection = new LDAPConnection(split[0], Integer.parseInt(split[1]));
          log.debug("Connected to LDAP host #" + (i + 1));
          break;
        } catch (Exception e) {
          // Do nothing.
        }
      }
    }

    if (connection == null)
      throw new RuntimeException(ERR_CONNECT_FAILED);

    return connection;
  }
}
