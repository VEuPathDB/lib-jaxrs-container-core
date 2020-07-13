package org.veupathdb.lib.container.jaxrs.view.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BadMethodTest
{
  @Test
  public void constructor1() {
    var foo = new BadMethodError();

    assertEquals(ErrorStatus.BAD_METHOD.toString(),
      foo.getStatus());
  }

  @Test
  public void constructor2() {
    var foo = new BadMethodError("error");

    assertEquals(ErrorStatus.BAD_METHOD.toString(),
      foo.getStatus());
    assertEquals("error", foo.getMessage());
  }

  @Test
  public void constructor3() {
    var foo = new BadMethodError(new Exception("foo"));

    assertEquals(ErrorStatus.BAD_METHOD.toString(),
      foo.getStatus());
    assertEquals("foo", foo.getMessage());
  }
}
