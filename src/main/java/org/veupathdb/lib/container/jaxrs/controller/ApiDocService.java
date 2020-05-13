package org.veupathdb.lib.container.jaxrs.controller;

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
    return getClass().getResourceAsStream("/api.html")::transferTo;
  }
}
