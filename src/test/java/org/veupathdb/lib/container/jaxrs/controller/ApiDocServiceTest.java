package org.veupathdb.lib.container.jaxrs.controller;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ApiDocServiceTest {

  @Test
  void streamApiDoc() throws IOException {
    var file = new File("build/resources/main/api.html");
    file.createNewFile();
    file.deleteOnExit();
    assertNotNull(new ApiDocService().getApi());
  }
}
