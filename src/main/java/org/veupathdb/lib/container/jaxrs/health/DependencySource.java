package org.veupathdb.lib.container.jaxrs.health;

import org.veupathdb.lib.container.jaxrs.providers.DependencyProvider;

/**
 * Defines a mechanism for supplying extra or ephemeral dependencies that cannot
 * or should not be registered as a permanent dependency.
 * <p>
 * This source is to be queried by {@link DependencyProvider#testDependencies()}
 * and any dependencies returned by this source will be tested at that time.
 */
public interface DependencySource extends Iterable<Dependency> {}
