package org.veupathdb.lib.container.jaxrs.utils.logging;

import java.io.IOException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.yaml.YamlConfiguration;

public class Log {

  /**
   * Routes all logging through Log4J2 and applies the configuration from
   * resources.
   * <p>
   * This is needed to hijack the default logging done by other libraries and
   * force them through log4j.
   */
  public static void initialize() {
    try {
      //noinspection resource
      Configurator.initialize(
        new YamlConfiguration((LoggerContext) LogManager.getContext(),
        new ConfigurationSource(Objects.requireNonNull(Log.class.getResourceAsStream("/log4j2.yml")))));

      LoggingVars.setNonRequestThreadVars("init0");
      LogManager.getLogger(Log.class).debug("Logger initialized");
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
