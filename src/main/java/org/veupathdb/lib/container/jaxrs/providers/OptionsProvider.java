package org.veupathdb.lib.container.jaxrs.providers;

import javax.ws.rs.ext.Provider;

import java.util.function.Supplier;

import org.veupathdb.lib.container.jaxrs.config.Options;

@Provider
public class OptionsProvider {
  private OptionsProvider() {}

  private static OptionsProvider instance;

  private static Supplier<Options> provider = OptionsProvider::defaultProvider;

  private Options options;

  public Options getOrCreateOptions() {
    if (options == null)
      options = provider.get();
    return options;
  }

  public static void setProvider(Supplier<Options> prov) {
    provider = prov;
  }

  public static Options getOptions() {
    return getInstance().getOrCreateOptions();
  }

  private static Options defaultProvider() { return new Options(); }

  private static OptionsProvider getInstance() {
    if (instance == null)
      instance = new OptionsProvider();
    return instance;
  }
}
