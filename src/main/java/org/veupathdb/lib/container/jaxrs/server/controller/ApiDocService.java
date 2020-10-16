package org.veupathdb.lib.container.jaxrs.server.controller;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;

@Path("/api")
public class ApiDocService {
  @GET
  @Produces(MediaType.TEXT_HTML)
  public StreamingOutput getApi() {
    return out -> {
      try (InputStream resourceStream = getClass().getResourceAsStream("/api.html")) {
        resourceStream.transferTo(out);
      }
    };
  }
}
