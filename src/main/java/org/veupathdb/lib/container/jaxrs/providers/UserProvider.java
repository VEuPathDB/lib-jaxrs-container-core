package org.veupathdb.lib.container.jaxrs.providers;

import java.util.Map.Entry;
import java.util.Optional;
import javax.ws.rs.core.Request;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

public class UserProvider {
  public static Optional<User> lookupUser(Request req) {
    return Optional.ofNullable((ContainerRequest)req)
      .map(r -> r.getProperty(Globals.REQUEST_USER))
      .map(User.class::cast);
  }

  public static Optional<Entry<String,String>> getSubmittedAuth(Request req) {
    return Optional.ofNullable((ContainerRequest)req)
      .map(r -> new TwoTuple<String,String>(
          RequestKeys.AUTH_HEADER, (String)r.getProperty(Globals.SUBMITTED_AUTH_KEY)));
  }
}
