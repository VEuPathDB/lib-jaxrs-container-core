package org.veupathdb.lib.container.jaxrs.providers;

/**
 * Mockable wrapper for the Java Runtime class.
 */
public class RuntimeProvider {
  private static RuntimeProvider instance;

  private RuntimeProvider() {}

  public Runtime getRuntime() {
    return Runtime.getRuntime();
  }

  public static Runtime runtime() {
    return getInstance().getRuntime();
  }

  public static RuntimeProvider getInstance() {
    if (instance == null)
      instance = new RuntimeProvider();
    return instance;
  }
}
