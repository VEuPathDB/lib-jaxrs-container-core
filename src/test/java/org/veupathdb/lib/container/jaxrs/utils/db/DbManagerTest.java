package org.veupathdb.lib.container.jaxrs.utils.db;

import org.gusdb.fgputil.db.platform.SupportedPlatform;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DbManagerTest {

  @BeforeEach
  void setUp() throws Exception {
    var dbManIn = DbManager.class.getDeclaredField("instance");
    dbManIn.setAccessible(true);
    dbManIn.set(null, null);
  }

  @Test
  void getAccountDatabaseExists() throws Exception {
    var db = DbManager.getInstance();
    var mk = mock(DatabaseInstance.class);
    var fd = DbManager.class.getDeclaredField("acctDb");
    fd.setAccessible(true);
    fd.set(db, mk);

    assertSame(mk, DbManager.accountDatabase());
  }

  @Test
  void getAccountDatabaseNotExists() throws Exception {
    var db = DbManager.getInstance();
    var fd = DbManager.class.getDeclaredField("acctDb");
    fd.setAccessible(true);
    fd.set(db, null);

    assertThrows(IllegalStateException.class, DbManager::accountDatabase);
  }

  @Test
  void makeOracleJdbcUrl() {
    var opts = new DbOptions();
    var dbMan  = DbManager.getInstance();

    opts.host = "";
    opts.name = "";

    assertNotNull(dbMan.makeOracleJdbcUrl(opts));
  }

  @Test
  void makePostgresJdbcUrl() {
    var opts = new DbOptions();
    var dbMan  = DbManager.getInstance();

    opts.host = "";
    opts.name = "";

    assertNotNull(dbMan.makePostgresJdbcUrl(opts));
  }

  @Test
  void usesCorrectPlatform() {
    var opts  = new DbOptions();
    var test  = mock(DbManager.class);

    opts.user = "foo";
    opts.pass = "bar";
    opts.platform = SupportedPlatform.ORACLE;

    when(test.makeJdbcUrl(SupportedPlatform.ORACLE, opts))
      .thenReturn("oracle");
    when(test.makeJdbcUrl(SupportedPlatform.POSTGRESQL, opts))
      .thenReturn("postgres");
    when(test.initDbConfig(opts)).thenCallRealMethod();

    var out = test.initDbConfig(opts);

    assertEquals("oracle", out.getConnectionUrl());
    assertEquals("foo", out.getLogin());
    assertEquals("bar", out.getPassword());

    opts.platform = SupportedPlatform.POSTGRESQL;

    out = test.initDbConfig(opts);

    assertEquals(out.getConnectionUrl(), "postgres");
  }

  @Test
  void makeJdbcUrl() {
    var opts = new DbOptions();
    var test = mock(DbManager.class);
    when(test.makeJdbcUrl(any(SupportedPlatform.class), same(opts)))
      .thenCallRealMethod();
    when(test.makeOracleJdbcUrl(opts)).thenReturn("foo");
    when(test.makePostgresJdbcUrl(opts)).thenReturn("bar");

    assertEquals("foo", test.makeJdbcUrl(SupportedPlatform.ORACLE, opts));
    assertEquals("bar", test.makeJdbcUrl(SupportedPlatform.POSTGRESQL, opts));
  }
}
