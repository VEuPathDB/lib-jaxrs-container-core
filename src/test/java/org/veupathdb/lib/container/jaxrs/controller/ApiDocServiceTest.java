package org.veupathdb.lib.container.jaxrs.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiDocServiceTest {

  @Test
  void getApi() {
    var test = new ApiDocService();
    assertNotNull(test.getApi());
  }

  @Test
  void streamApiDoc() {
    assertNotNull(new ApiDocService().getApi());
  }
}
