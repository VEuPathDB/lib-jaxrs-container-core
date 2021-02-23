package org.veupathdb.lib.container.jaxrs.providers;

import org.glassfish.jersey.server.ContainerRequest;

import javax.ws.rs.core.Request;

import java.util.Optional;

import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.model.User;

public class UserProvider {
  public static Optional<User> lookupUser(Request req) {
    return Optional.ofNullable((ContainerRequest)req)
      .map(r -> r.getProperty(Globals.REQUEST_USER))
      .map(User.class::cast);
  }
}
