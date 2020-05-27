package org.veupathdb.lib.container.jaxrs.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.veupathdb.lib.container.jaxrs.health.Dependency.Status;
import org.veupathdb.lib.container.jaxrs.health.Dependency.TestResult;
import org.veupathdb.lib.container.jaxrs.utils.net.Pinger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ServiceDependencyTest {

  Pinger pinger;

  @BeforeEach
  void setUp() {
    pinger = mock(Pinger.class);
  }

  @Test
  void getUrl() {
    var test = new ServiceDependency("", "foo", 0) {
      @Override protected TestResult serviceTest() { return  null; }
    };

    assertEquals("foo", test.getUrl());
  }

  @Test
  void getPort() {
    var test = new ServiceDependency("", "", 666) {
      @Override protected TestResult serviceTest() { return  null; }
    };

    assertEquals(666, test.getPort());
  }

  @Test
  void close() {
    var test = new ServiceDependency("", "foo", 321) {
      @Override protected TestResult serviceTest() { return null; }
    };
    test.close(); // Empty method, nothing to test.
  }

  @Nested
  class TestFn {

    @Test
    void testNoPing() {
      when(pinger.isReachable("foo", 321)).thenReturn(false);

      var test = new ServiceDependency("", "foo", 321) {
        @Override protected TestResult serviceTest() { return null; }
      };
      test.setPinger(pinger);

      var res = test.test();

      assertFalse(res.isReachable());
      assertEquals(Status.UNKNOWN, res.status());
    }

    @Test
    void testPassthrough() {
      when(pinger.isReachable("foo", 321)).thenReturn(true);

      var value = new TestResult(null, true, Status.UNKNOWN);

      var test = new ServiceDependency("", "foo", 321) {
        @Override protected TestResult serviceTest() { return value; }
      };

      test.setPinger(pinger);

      System.out.println(test.test().isReachable());
      System.out.println(test.test().status());

      assertSame(value, test.test());
    }
  }
}
