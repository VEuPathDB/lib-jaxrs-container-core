package org.veupathdb.lib.container.jaxrs.providers;

import javax.ws.rs.core.Request;

import org.glassfish.jersey.server.ContainerRequest;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

public class RequestIdProvider
{
  public static String getRequestId(Request req) {
    return (String) ((ContainerRequest)req).getProperty(RequestKeys.REQUEST_ID);
  }
}
