package org.veupathdb.lib.container.jaxrs.server.middleware;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

public class CorsFilter implements ContainerResponseFilter
{
  private static final String[][] headers = {
    {"Access-Control-Allow-Origin", "*"},
    {"Access-Control-Allow-Headers", "origin, content-type, accept, authorization, Auth-Key"},
    {"Access-Control-Allow-Credentials", "true"},
    {"Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD"},
  };

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    var head = res.getHeaders();
    for (var pair : headers)
      head.add(pair[0], pair[1]);
  }
}
