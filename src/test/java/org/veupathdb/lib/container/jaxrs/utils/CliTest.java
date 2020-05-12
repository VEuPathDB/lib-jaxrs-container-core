package org.veupathdb.lib.container.jaxrs.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.internal.verification.Times;
import picocli.CommandLine.Option;

import org.veupathdb.lib.container.jaxrs.providers.RuntimeProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CliTest {

  @Nested
  class ParseCLI {

    private class Foo {
      @Option(names = "-a")
      String a;

      @Option(names = "-b")
      String b;
    }

    @Test
    void success() {
      var test = new Foo();
      Cli.parseCLI(new String[] {"-a", "pple", "-b", "anana"}, test);

      assertEquals("pple", test.a);
      assertEquals("anana", test.b);
    }

    @Test
    void unknownParam() {
      var test = new Foo();

      assertThrows(RuntimeException.class,
        () -> Cli.parseCLI(new String[] {"-c", "arp"}, test));
    }

    @Test
    void helpParam() throws Exception {
      var mockStatics = mock(RuntimeProvider.class);
      var mockRuntime = mock(Runtime.class);

      when(mockStatics.getRuntime()).thenReturn(mockRuntime);
      doNothing().when(mockRuntime).exit(0);

      var f = RuntimeProvider.class.getDeclaredField("instance");
      f.setAccessible(true);
      f.set(null, mockStatics);

      var test = new Foo();

      try { Cli.parseCLI(new String[] {"-h"}, test); }
      catch (Throwable ignored) {}

      verify(mockRuntime, new Times(1)).exit(0);

      try { Cli.parseCLI(new String[] {"--help"}, test); }
      catch (Throwable ignored) {}

      verify(mockRuntime, new Times(2)).exit(0);
    }
  }

  @Nested
  class EmptyToNull {
    private class Bar {
      private String a;
      private String b;
      private Integer c;
      private Integer d;
      @SuppressWarnings("unused")
      private Object  e;
    }

    @Test
    void success() {
      var test = new Bar();
      test.a = "";
      test.b = "foo";
      test.c = 0;
      test.d = 10;

      Cli.emptyToNull(test);

      assertNull(test.a);
      assertEquals("foo", test.b);
      assertNull(test.c);
      assertEquals(10, test.d);
      assertNull(test.e);
    }
  }
}
