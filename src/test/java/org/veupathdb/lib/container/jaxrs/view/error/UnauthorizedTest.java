package org.veupathdb.lib.container.jaxrs.view.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UnauthorizedTest
{
  @Test
  public void constructor1() {
    var foo = new UnauthorizedError();

    assertEquals(ErrorStatus.UNAUTHORIZED.toString(),
      foo.getStatus());
  }

  @Test
  public void constructor2() {
    var foo = new UnauthorizedError("error");

    assertEquals(ErrorStatus.UNAUTHORIZED.toString(),
      foo.getStatus());
    assertEquals("error", foo.getMessage());
  }

  @Test
  public void constructor3() {
    var foo = new UnauthorizedError(new Exception("foo"));

    assertEquals(ErrorStatus.UNAUTHORIZED.toString(),
      foo.getStatus());
    assertEquals("foo", foo.getMessage());
  }
}
