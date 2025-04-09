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

    var bar = new ConnectionDetailsImpl(opts);

    assertEquals("name", bar.dbName());
    assertEquals("host", bar.host());
    assertEquals("pass", bar.password());
    assertEquals(1, bar.poolSize());
    assertEquals(123, bar.port());
    assertEquals("user", bar.user());

    assertNotNull(bar.toJdbcString());
    assertFalse(bar.toJdbcString().isBlank());

    var conf = bar.toFgpUtilConfig();

    assertEquals(bar.toJdbcString(), conf.getConnectionUrl());
    assertEquals(bar.user(), conf.getLogin());
    assertEquals(bar.password(), conf.getPassword());
    assertEquals(SupportedPlatform.POSTGRESQL, conf.getPlatformEnum());
  }



}
