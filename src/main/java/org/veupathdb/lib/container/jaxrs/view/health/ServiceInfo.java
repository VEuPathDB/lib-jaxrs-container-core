package org.veupathdb.lib.container.jaxrs.view.health;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.time.Duration;

public class ServiceInfo {
  private int threads;
  private long uptime;

  @JsonGetter
  public int getThreads() {
    return threads;
  }

  public ServiceInfo setThreads(int threads) {
    this.threads = threads;
    return this;
  }

  @JsonGetter
  public String getUptime() {
    var tmp = Duration.ofMillis(uptime);
    var days = tmp.toDaysPart();
    var hours = tmp.toHoursPart();
    var minutes = tmp.toMinutesPart();
    var seconds = tmp.toSecondsPart();
    var millis = tmp.toMillisPart();
    if (days > 0)
      return String.format("%dd %dh %dm %d.%ds", days, hours, minutes, seconds, millis);
    if (hours > 0)
      return String.format("%dh %dm %d.%ds", hours, minutes, seconds, millis);
    if (minutes > 0)
      return String.format("%dm %d.%ds", minutes, seconds, millis);
    return String.format("%d.%ds", seconds, millis);
  }

  @JsonGetter
  public long getUptimeMillis() {
    return uptime;
  }

  public ServiceInfo setUptime(long uptime) {
    this.uptime = uptime;
    return this;
  }
}
