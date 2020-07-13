package org.veupathdb.lib.container.jaxrs.view.error;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BadRequestTest
{
  @Test
  public void constructor1() {
    var foo = new BadRequestError();

    assertEquals(ErrorStatus.BAD_REQUEST.toString(),
      foo.getStatus());
  }

  @Test
  public void constructor2() {
    var foo = new BadRequestError("error");

    assertEquals(ErrorStatus.BAD_REQUEST.toString(),
      foo.getStatus());
    assertEquals("error", foo.getMessage());
  }

  @Test
  public void constructor3() {
    var foo = new BadRequestError(new Exception("foo"));

    assertEquals(ErrorStatus.BAD_REQUEST.toString(),
      foo.getStatus());
    assertEquals("foo", foo.getMessage());
  }
}
