package org.veupathdb.lib.container.jaxrs.utils;

import com.devskiller.friendly_id.FriendlyId;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;
import org.veupathdb.lib.container.jaxrs.Globals;

import java.io.IOException;

public class Log {
  /**
   * Routes all logging through Log4J2 and applies the configuration from
   * resources.
   *
   * This is needed to hijack the default logging done by other libraries and
   * force them through log4j.
   */
  public static void initialize() {
    try {
//      java.util.logging.LogManager.getLogManager().reset();

      Configurator.initialize(
        new YamlConfiguration((LoggerContext) LogManager.getContext(),
        new ConfigurationSource(Log.class.getResourceAsStream("/log4j2.yml"))));

    ThreadContext.put(Globals.CONTEXT_ID, FriendlyId.createFriendlyId());
    LogManager.getLogger(Log.class).debug("Logger initialized");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
