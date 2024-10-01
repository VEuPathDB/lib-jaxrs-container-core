package org.veupathdb.lib.container.jaxrs.server.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.StreamingOutput;

import java.io.InputStream;
import java.util.Objects;

@Path("/api")
public class ApiDocService {
  @GET
  @Produces(MediaType.TEXT_HTML)
  public StreamingOutput getApi() {
    return out -> {
      try (InputStream resourceStream = getClass().getResourceAsStream("/api.html")) {
        Objects.requireNonNull(resourceStream).transferTo(out);
      }
    };
  }
}
