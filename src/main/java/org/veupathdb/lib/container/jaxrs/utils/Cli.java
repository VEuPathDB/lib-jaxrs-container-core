package org.veupathdb.lib.container.jaxrs.utils;

import picocli.CommandLine;
import picocli.CommandLine.UnmatchedArgumentException;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Predicate;

import org.veupathdb.lib.container.jaxrs.providers.RuntimeProvider;

public final class Cli {
  private Cli() {}

  /**
   * Parses the given arguments into the given annotated type.
   *
   * @param args   CLI Input arguments
   * @param config Configuration object with PicoCLI annotated properties.
   * @param <T>    Type of the input config object
   *
   * @return       Passthrough of the input config object
   */
  public static <T> T parseCLI(String[] args, T config) {
    var cli = new CommandLine(config)
      .setCaseInsensitiveEnumValuesAllowed(true)
      .setUnmatchedArgumentsAllowed(false);
    try {
      cli.parseArgs(args);
    } catch (UnmatchedArgumentException e) {
      var match = e.getUnmatched()
        .stream()
        .anyMatch(((Predicate<String>)"-h"::equals).or("--help"::equals));

      if (match) {
        cli.usage(System.out);
        RuntimeProvider.runtime().exit(0);
      }

      throw new RuntimeException("Unrecognized argument(s) " + e.getUnmatched());
    }

    emptyToNull(config);

    return config;
  }

  /**
   * Reflectively converts empty values in the CLI config to null to play nicely
   * with the Optional type.
   *
   * @param opts Object to process.
   */
  static <T> void emptyToNull(T opts) {
    try {
      for (var prop : opts.getClass().getDeclaredFields()) {
        var mods = prop.getModifiers();
        if (Modifier.isFinal(mods))
          continue;

        prop.setAccessible(true);

        var tmp = prop.get(opts);

        if (Objects.isNull(tmp))
          continue;

        if (tmp.equals("")) {
          prop.set(opts, null);
        } else if (tmp.equals(0)) {
          prop.set(opts, null);
        }
      }
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }
}
