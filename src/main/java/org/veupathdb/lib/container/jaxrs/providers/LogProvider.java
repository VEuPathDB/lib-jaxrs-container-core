package org.veupathdb.lib.container.jaxrs.providers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mockable Wrapper for SLF4J's {@link LoggerFactory}.
 */
public class LogProvider {
  private static LogProvider instance;

  private LogProvider() {}

  public Logger getLogger(Class<?> type) {
    return LoggerFactory.getLogger(type);
  }

  public Logger getLogger(String name) {
    return LoggerFactory.getLogger(name);
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
