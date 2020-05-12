package org.veupathdb.lib.container.jaxrs.providers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mockable Wrapper for the Log4j log manager.
 */
public class LogProvider {
  private static LogProvider instance;

  private LogProvider() {}

  public Logger getLogger(Class<?> type) {
    return LogManager.getLogger(type);
  }

  public Logger getLogger(String name) {
    return LogManager.getLogger(name);
  }

  public static Logger logger(Class<?> type) {
    return getInstance().getLogger(type);
  }

  public static Logger logger(String name) {
    return getInstance().getLogger(name);
  }

  public static LogProvider getInstance() {
    if (instance == null)
      instance = new LogProvider();
    return instance;
  }
}
