package org.veupathdb.lib.container.jaxrs.providers;

import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.accountdb.UserProfile;

import javax.ws.rs.core.Request;

import java.util.Optional;

import org.veupathdb.lib.container.jaxrs.Globals;

public class UserProvider {
  public static Optional<UserProfile> lookupUser(Request req) {
    return Optional.ofNullable((ContainerRequest)req)
      .map(r -> r.getProperty(Globals.REQUEST_USER))
      .map(UserProfile.class::cast);
  }
}
