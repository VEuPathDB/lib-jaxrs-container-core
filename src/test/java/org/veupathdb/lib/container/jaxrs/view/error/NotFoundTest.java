package org.veupathdb.lib.container.jaxrs.view.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotFoundTest
{
  @Test
  public void constructor1() {
    var foo = new NotFoundError();

    assertEquals(ErrorStatus.NOT_FOUND.toString(),
      foo.getStatus());
  }

  @Test
  public void constructor2() {
    var foo = new NotFoundError("error");

    assertEquals(ErrorStatus.NOT_FOUND.toString(),
      foo.getStatus());
    assertEquals("error", foo.getMessage());
  }

  @Test
  public void constructor3() {
    var foo = new NotFoundError(new Exception("foo"));

    assertEquals(ErrorStatus.NOT_FOUND.toString(),
      foo.getStatus());
    assertEquals("foo", foo.getMessage());
  }
}
