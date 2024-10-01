package org.veupathdb.lib.container.jaxrs.providers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.veupathdb.lib.container.jaxrs.errors.DuplicateDependencyException;
import org.veupathdb.lib.container.jaxrs.health.Dependency;
import org.veupathdb.lib.container.jaxrs.health.Dependency.TestResult;

public class DependencyProvider {
  private static DependencyProvider instance;

  private final Map<String, Dependency> dependencies;

  private final Logger log = LogProvider.logger(DependencyProvider.class);

  private DependencyProvider() {
    dependencies = new HashMap<>();
  }

  /**
   * Attempts to fetch a dependency by name.
   *
   * @return An option that will be empty if no dependency was found with the
   *         given name.
   */
  public synchronized Optional<Dependency> lookup(String name) {
    return Optional.ofNullable(dependencies.get(name));
  }

  /**
   * Returns whether or not there is a dependency registered with the given
   * name.
   */
  public synchronized boolean has(String name) {
    return dependencies.containsKey(name);
  }

  /**
   * Register a new dependency.
   */
  public synchronized void register(Dependency dep) {
    var name = dep.getName();
    var cur  = dependencies.get(name);

    if (cur != null && cur != dep)
      throw new DuplicateDependencyException(name);

    dependencies.put(name, dep);
  }

  /**
   * Removes and closes the dependency with the given name.
   *
   * If no dependency was found with the given name, this method does nothing.
   */
  public synchronized void closeAndRemove(String name) throws Exception {
    if (has(name))
      dependencies.remove(name).close();
  }

  /**
   * Runs the test method on all currently registered dependencies and returns
   * a map of the test results keyed on dependency name.
   */
  public List<TestResult> testDependencies() {
    return dependencies.values()
      .parallelStream()
      .map(Dependency::test)
      .collect(Collectors.toUnmodifiableList());
  }

  /**
   * Attempts to shut down all dependencies currently registered.
   */
  public void shutDown() {
    for(var dep : dependencies.values()) {
      try {
        dep.close();
      } catch (Exception e) {
        log.error("Failed to shut down dependency " + dep.getName(), e);
      }
    }
  }

  public static DependencyProvider getInstance() {
    if (instance == null)
      instance = new DependencyProvider();

    return instance;
  }
}
