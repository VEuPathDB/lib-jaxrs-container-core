package org.veupathdb.lib.container.jaxrs.health;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AbstractDependencyTest {

  @Test
  void testGetName() {
    var test = new AbstractDependency("FOO") {
      @Override public void close() {}
      @Override public TestResult test() { return null; }
    };
    assertEquals("FOO", test.getName());
  }
}
