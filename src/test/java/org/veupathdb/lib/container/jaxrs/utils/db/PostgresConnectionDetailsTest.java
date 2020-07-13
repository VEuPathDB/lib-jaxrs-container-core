package org.veupathdb.lib.container.jaxrs.utils.db;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.junit.jupiter.api.Test;
import org.veupathdb.lib.container.jaxrs.config.DbOptionsImpl;

import static org.junit.jupiter.api.Assertions.*;

public class PostgresConnectionDetailsTest
{
  @Test
  public void fromOptionsRaw() {
    var opts =  new DbOptionsImpl(
      null, "host", 123, "name", "user", "pass", SupportedPlatform.POSTGRESQL, 1,
      "displayName");

    var bar = PostgresConnectionDetails.fromOptions(opts);

    assertEquals(bar.dbName(), "name");
    assertEquals(bar.host(), "host");
    assertEquals(bar.password(), "pass");
    assertEquals(bar.poolSize(), 1);
    assertEquals(bar.port(), 123);
    assertEquals(bar.user(), "user");

    assertNotNull(bar.toJdbcString());
    assertFalse(bar.toJdbcString().isBlank());

    var conf = bar.toFgpUtilConfig();

    assertEquals(conf.getConnectionUrl(), bar.toJdbcString());
    assertEquals(conf.getLogin(), bar.user());
    assertEquals(conf.getPassword(), bar.password());
    assertEquals(conf.getPlatformEnum(), SupportedPlatform.POSTGRESQL);
  }



}
