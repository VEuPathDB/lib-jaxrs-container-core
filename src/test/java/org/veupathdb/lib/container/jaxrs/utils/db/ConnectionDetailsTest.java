package org.veupathdb.lib.container.jaxrs.utils.db;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.junit.jupiter.api.Test;
import org.veupathdb.lib.container.jaxrs.config.DbOptionsImpl;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionDetailsTest
{
  @Test
  public void fromOptionsPostgres() {
    var foo = new DbOptionsImpl(
      "tnsName", "host", 123, "name", "user", "pass",
      SupportedPlatform.POSTGRESQL, 1, "displayName");

    var bar = ConnectionDetails.fromOptions(foo);

    assertNotNull(bar);
    assertTrue(bar instanceof PostgresConnectionDetails);
  }

  @Test
  public void fromOptionsOracleRaw() {
    var foo = new DbOptionsImpl(
      null, "host", 123, "name", "user", "pass", SupportedPlatform.ORACLE, 1,
      "displayName");

    var bar = ConnectionDetails.fromOptions(foo);

    assertNotNull(bar);
    assertTrue(bar instanceof OracleConnectionDetails);
  }
}
