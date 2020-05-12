package org.veupathdb.lib.container.jaxrs.config;

public class InvalidConfigException extends RuntimeException {
  public InvalidConfigException(String message) {
    super(message);
  }
}
