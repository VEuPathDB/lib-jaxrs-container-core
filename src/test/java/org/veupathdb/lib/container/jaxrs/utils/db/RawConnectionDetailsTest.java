package org.veupathdb.lib.container.jaxrs.utils.db;

import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class RawConnectionDetailsTest
{
  private static class Type extends RawConnectionDetails {
    @Override public String toJdbcString() {return null;}
    @Override public ConnectionPoolConfig toFgpUtilConfig() {return null;}
  }

  @Test
  void host() {
    var test = new Type();
    assertSame(test, test.host("abcd"));
    assertEquals("abcd", test.host());
  }

  @Test
  void user() {
    var test = new Type();
    assertSame(test, test.user("abcd"));
    assertEquals("abcd", test.user());
  }

  @Test
  void dbName() {
    var test = new Type();
    assertSame(test, test.dbName("abcd"));
    assertEquals("abcd", test.dbName());
  }

  @Test
  void password() {
    var test = new Type();
    assertSame(test, test.pass("abcd"));
    assertEquals("abcd", test.password());
  }

  @Test
  void port() {
    var test = new Type();
    assertSame(test, test.port(1234));
    assertEquals(1234, test.port());
  }

  @Test
  void poolSize() {
    var test = new Type();
    assertSame(test, test.poolSize(1234));
    assertEquals(1234, test.poolSize());
  }
}
