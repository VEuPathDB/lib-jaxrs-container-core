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
    return findRequestProp(req, User.class, Globals.REQUEST_USER);
  }

  public static Optional<TwoTuple<String, String>> getSubmittedAuth(ContainerRequest req) {
    return
      // look for property on request
      findRequestProp(req, String.class, RequestKeys.AUTH_HEADER)
      // auth may not be enabled; look for auth value independently
      .or(() -> AuthFilter.findSubmittedValue(req, RequestKeys.AUTH_HEADER))
      // convert value to Entry if present
      .map(s -> new TwoTuple<>(RequestKeys.AUTH_HEADER, s));
  }

  private static <T> Optional<T> findRequestProp(ContainerRequest req, Class<T> clazz, String key) {
    return Optional.ofNullable(
            // caller must pass a non-null request
            Objects.requireNonNull(req))
        // try to get specified request property
        .map(r -> r.getProperty(key))
        // cast to the requested type
        .map(clazz::cast);
  }
}
