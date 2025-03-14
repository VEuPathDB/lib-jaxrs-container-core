package org.veupathdb.lib.container.jaxrs.health;

import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.veupathdb.lib.container.jaxrs.health.Dependency.Status;
import org.veupathdb.lib.container.jaxrs.utils.net.Pinger;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FgpDatabaseDependencyTest
{

  @Nested
  class TestFn {

    Pinger pinger;
    DatabaseInstance db;
    DataSource ds;
    Connection con;
    Statement stmt;

    @BeforeEach
    void setUp() {
      pinger = mock(Pinger.class);
      db     = mock(DatabaseInstance.class);
      ds     = mock(DataSource.class);
      con    = mock(Connection.class);
      stmt   = mock(Statement.class);

      when(db.getDataSource()).thenReturn(ds);
    }

    @Test
    void success() throws Exception {
      when(pinger.isReachable("foo", 123)).thenReturn(true);
      when(ds.getConnection()).thenReturn(con);
      when(con.createStatement()).thenReturn(stmt);
      //noinspection SqlNoDataSourceInspection
      when(stmt.execute("SELECT 1 FROM DUAL")).thenReturn(true);

      var test = new FgpDatabaseDependency("", "foo", 123, db);

      test.setPinger(pinger);

      var res  = test.test();

      assertTrue(res.reachable);
      assertEquals(Status.ONLINE, res.status);
    }

    @Test
    void noConnection() throws Exception {
      when(pinger.isReachable("foo", 123)).thenReturn(true);
      when(ds.getConnection()).thenThrow(new SQLException());

      var test = new FgpDatabaseDependency("", "foo", 123, db);

      test.setPinger(pinger);

      var res  = test.test();

      assertTrue(res.reachable);
      assertEquals(Status.UNKNOWN, res.status);
    }

    @Test
    void noStatement() throws Exception {
      when(pinger.isReachable("foo", 123)).thenReturn(true);
      when(ds.getConnection()).thenReturn(con);
      when(con.createStatement()).thenThrow(new SQLException());

      var test = new FgpDatabaseDependency("", "foo", 123, db);

      test.setPinger(pinger);

      var res  = test.test();

      assertTrue(res.reachable);
      assertEquals(Status.UNKNOWN, res.status);
    }


    @Test
    void noPing() {
      when(pinger.isReachable("foo", 123)).thenReturn(false);

      var test = new FgpDatabaseDependency("", "foo", 123, db);

      test.setPinger(pinger);

      var res  = test.test();

      assertFalse(res.reachable);
      assertEquals(Status.UNKNOWN, res.status);
    }
  }

  @Test
  void close() throws Exception {
    var db   = mock(DatabaseInstance.class);
    var test = new FgpDatabaseDependency("", "", 0, db);
    test.close();
    verify(db).close();

    doThrow(new AbstractMethodError()).when(db).close();
    assertThrows(AbstractMethodError.class, test::close);
  }

  @Test
  void setTestQuery() throws Exception {
    var test = new FgpDatabaseDependency("", "", 0, null);
    var val  = "some query";
    test.setTestQuery(val);

    var field = FgpDatabaseDependency.class.getDeclaredField("testQuery");
    field.setAccessible(true);

    assertEquals(val, field.get(test));
  }
}
