package org.veupathdb.lib.container.jaxrs.view.error;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class ErrorResponseTest
{
  @ParameterizedTest
  @EnumSource(ErrorStatus.class)
  public void constructor(ErrorStatus status) {
    var foo = new ErrorResponse(status);

    assertEquals(status.toString(), foo.getStatus());
  }

  @Test
  public void message() {
    var foo = new ErrorResponse(null);

    assertSame(foo, foo.setMessage("bar"));
    assertEquals("bar", foo.getMessage());
  }
}
