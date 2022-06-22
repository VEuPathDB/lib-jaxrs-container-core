package org.veupathdb.lib.container.jaxrs.providers;

import java.util.Objects;
import java.util.Optional;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.server.middleware.AuthFilter;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

public class UserProvider {

  public static Optional<User> lookupUser(ContainerRequest req) {
    return Optional.ofNullable(req)
      .map(r -> r.getProperty(Globals.REQUEST_USER))
      .map(User.class::cast);
  }

  public static Optional<TwoTuple<String, String>> getSubmittedAuth(ContainerRequest req) {
    String auth = Optional.ofNullable(Objects
      // caller must pass a non-null request
      .requireNonNull(req)
      // try to get auth as request property (put there if auth is enabled)
      .getProperty(RequestKeys.AUTH_HEADER)
    )
    // apply cast to String
    .map(String.class::cast)
    // auth may not be enabled; look for auth value independently
    .orElse(AuthFilter.findAuthValue(req).orElse(null));

    // convert value to Entry if present
    return Optional.ofNullable(auth)
      .map(s -> new TwoTuple<>(RequestKeys.AUTH_HEADER, s));
  }
}
