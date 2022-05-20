package org.veupathdb.lib.container.jaxrs.utils.db;

import io.prometheus.client.Gauge;
import org.gusdb.fgputil.db.pool.DatabaseInstance;

import java.util.ArrayList;
import java.util.List;

public final class DBPrometheus {

  private static final int resolutionMillis = 5000;

  private static final List<GaugeSet> openConnections = new ArrayList<>(3);

  static {
    new Thread(() -> {
      while (true) {
        for (var p : openConnections) {
          p.active.set(p.db.getActiveCount());
          p.idle.set(p.db.getIdleCount());
        }

        try {
          Thread.sleep(resolutionMillis);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).start();
  }

  public static void register(String name, DatabaseInstance db) {
    openConnections.add(new GaugeSet(
      Gauge.build().
        name("db_active_connections_" + safeName(name)).
        help("Number of active database connections.").
        register(),
      Gauge.build().
        name("db_idle_connections_" + safeName(name)).
        help("Number of idle database connections.").
        register(),
      db
    ));
  }

  private static String safeName(String name) {
    return name.replace('-', '_');
  }

  private static class GaugeSet {
    final Gauge active;

    final Gauge idle;

    final DatabaseInstance db;

    public GaugeSet(
      Gauge active,
      Gauge idle,
      DatabaseInstance db
    ) {
      this.active = active;
      this.idle   = idle;
      this.db     = db;
    }
  }
}
