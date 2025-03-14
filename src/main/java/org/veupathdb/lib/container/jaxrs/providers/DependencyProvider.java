package org.veupathdb.lib.container.jaxrs.providers;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.slf4j.Logger;
import org.veupathdb.lib.container.jaxrs.errors.DuplicateDependencyException;
import org.veupathdb.lib.container.jaxrs.health.Dependency;
import org.veupathdb.lib.container.jaxrs.health.Dependency.TestResult;
import org.veupathdb.lib.container.jaxrs.health.DependencySource;

public class DependencyProvider {
  private static DependencyProvider instance;

  private final Map<String, Dependency> dependencies;

  private final List<DependencySource> extraDependencySources;

  private final Logger log = LogProvider.logger(DependencyProvider.class);

  private DependencyProvider() {
    dependencies = new HashMap<>();
    extraDependencySources = new ArrayList<>(2);
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
   * Returns whether there is a dependency registered with the given name.
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
   * <p>
   * If no dependency was found with the given name, this method does nothing.
   */
  public synchronized void closeAndRemove(String name) throws Exception {
    if (has(name))
      dependencies.remove(name).close();
  }

  /**
   * Registers a source of extra or ephemeral dependencies that should not or
   * cannot be registered normally.
   *
   * @param source Extra dependency source.
   */
  public synchronized void registerDependencySource(DependencySource source) {
    extraDependencySources.add(source);
  }

  /**
   * Runs the test method on all currently registered dependencies and returns
   * a map of the test results keyed on dependency name.
   */
  public List<TestResult> testDependencies() {
    return Stream.concat(
      Stream.of(dependencies.values()),
      extraDependencySources.stream()
    )
      .flatMap(it -> StreamSupport.stream(it.spliterator(), false))
      .parallel()
      .map(Dependency::test)
      .toList();
  }

  /**
   * Attempts to shut down all dependencies currently registered.
   */
  public void shutDown() {
    for(var dep : dependencies.values()) {
      try {
        dep.close();
      } catch (Exception e) {
        log.error("Failed to shut down dependency {}", dep.getName(), e);
      }
    }
  }

  public static DependencyProvider getInstance() {
    if (instance == null)
      instance = new DependencyProvider();

    return instance;
  }
}
