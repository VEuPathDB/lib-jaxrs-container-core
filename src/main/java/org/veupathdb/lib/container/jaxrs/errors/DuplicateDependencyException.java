package org.veupathdb.lib.container.jaxrs.errors;

public class DuplicateDependencyException extends RuntimeException {
  private static final String message = "Attempted to set more than one "
    + "dependency with the name %s";

  public DuplicateDependencyException(String name) {
    super(String.format(message, name));
  }
}
