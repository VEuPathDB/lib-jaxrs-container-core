package org.veupathdb.lib.container.jaxrs.utils.db;

import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

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
}
