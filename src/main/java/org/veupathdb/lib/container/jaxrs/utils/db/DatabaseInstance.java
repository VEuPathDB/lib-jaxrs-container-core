package org.veupathdb.lib.container.jaxrs.utils.db;

import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;

public class DatabaseInstance extends org.gusdb.fgputil.db.pool.DatabaseInstance {
  private boolean closed = false;

  public DatabaseInstance(ConnectionPoolConfig dbConfig, String identifier) {
    super(dbConfig, identifier);
  }

  public boolean isClosed() {
    return this.closed;
  }

  @Override
  public void close() throws Exception {
    closed = true;
    super.close();
  }
}
