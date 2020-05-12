package org.veupathdb.lib.container.jaxrs.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.veupathdb.lib.container.jaxrs.health.Dependency.Status;
import org.veupathdb.lib.container.jaxrs.health.Dependency.TestResult;
import org.veupathdb.lib.container.jaxrs.providers.DependencyProvider;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DependencyProviderTest {

  DependencyProvider test;

  @BeforeEach
  void setUp() throws Exception {
    var m = DependencyProvider.class.getDeclaredConstructor();
    m.setAccessible(true);

    test = m.newInstance();
  }

  @Test
  @SuppressWarnings("unchecked")
  void register() throws Exception {
    var dep = new DatabaseDependency("foo", "", 123, null);
    test.register(dep);

    var f = test.getClass().getDeclaredField("dependencies");
    f.setAccessible(true);

    var foo = (Map<String, Dependency>) f.get(test);

    assertSame(foo.get("foo"), dep);

    assertThrows(RuntimeException.class, () -> test.register(dep));
  }

  @Test
  void testDependencies() {
    var dep = mock(DatabaseDependency.class);
    var val = new TestResult(dep, false, Status.ONLINE);
    when(dep.getName()).thenReturn("foo");
    when(dep.test()).thenReturn(val);

    test.register(dep);
    var res = test.testDependencies();
    assertSame(res.get(0), val);
  }

  @Test
  void shutDown() throws Exception {
    var dep1 = mock(DatabaseDependency.class);
    var dep2 = mock(DatabaseDependency.class);

    when(dep1.getName()).thenReturn("foo");
    when(dep2.getName()).thenReturn("bar");

    doThrow(new Exception()).when(dep2).close();

    test.register(dep1);
    test.register(dep2);
    test.shutDown();

    verify(dep1).close();
    verify(dep2).close();
  }

  @Test
  void getInstance() {
    assertSame(DependencyProvider.getInstance(), DependencyProvider.getInstance());
  }
}
