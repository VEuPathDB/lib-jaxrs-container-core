package org.veupathdb.lib.container.jaxrs.utils.db;

import io.prometheus.client.Gauge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public final class DBPrometheus {

  private static final Logger log = LoggerFactory.getLogger(DBPrometheus.class);

  private static final int resolutionMillis = 5000;

  private static final List<GaugeSet> openConnections = new ArrayList<>(3);

  private static final Thread pollingThread;

  static {
    // Build the polling thread now, but don't start it yet as if it is started
    // before anything is added to the `openConnections` list, it will shut down
    // immediately.
    //
    // This thread will be started when the first connection is added to the
    // `openConnections` list.
    pollingThread = new Thread(() -> {
      while (true) {
        openConnections.removeIf(g -> g.db.isClosed());

        if (openConnections.isEmpty()) {
          log.info("all tracked database connections have been closed, shutting down metric collector");
          return;
        }

        for (var p : openConnections) {
          if (!p.db.isClosed()) {
            p.active.set(p.db.getActiveCount());
            p.idle.set(p.db.getIdleCount());
          }
        }

        try {
          //noinspection BusyWait
          Thread.sleep(resolutionMillis);
        } catch (InterruptedException e) {
          log.error("interrupt", e);
        }
      }
    });
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

    // If this was the first connection added to the registry, start up the
    // polling thread.
    if (openConnections.size() == 1)
      pollingThread.start();
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
