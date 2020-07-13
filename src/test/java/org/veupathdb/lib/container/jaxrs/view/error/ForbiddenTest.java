package org.veupathdb.lib.container.jaxrs.view.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForbiddenTest
{
  @Test
  public void constructor1() {
    var foo = new ForbiddenError();

    assertEquals(ErrorStatus.FORBIDDEN.toString(),
      foo.getStatus());
  }

  @Test
  public void constructor2() {
    var foo = new ForbiddenError("error");

    assertEquals(ErrorStatus.FORBIDDEN.toString(),
      foo.getStatus());
    assertEquals("error", foo.getMessage());
  }

  @Test
  public void constructor3() {
    var foo = new ForbiddenError(new Exception("foo"));

    assertEquals(ErrorStatus.FORBIDDEN.toString(),
      foo.getStatus());
    assertEquals("foo", foo.getMessage());
  }
}
