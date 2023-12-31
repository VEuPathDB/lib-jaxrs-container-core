package org.veupathdb.lib.container.jaxrs.providers;

import java.util.Objects;
import java.util.Optional;

import jakarta.ws.rs.core.HttpHeaders;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.oauth2.client.ValidatedToken;
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
    // caller must pass a non-null request
    Objects.requireNonNull(req);

    // look for bearer token on request first
    String token = (String)req.getProperty(HttpHeaders.AUTHORIZATION);
    // auth may not be enabled; if missing look for token value independently
    if (token == null) {
      token = AuthFilter.findBearerToken(req).map(obj -> obj.getTokenValue()).orElse(null);
    }
    // if found using either method, return header name/value
    if (token != null) {
      return Optional.of(new TwoTuple<>(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }

    // fall back to legacy auth (WDK cookie value or guest ID)
    String auth = (String)req.getProperty(RequestKeys.AUTH_HEADER_LEGACY);
    // auth may not be enabled; look for auth value independently
    if (auth == null) {
      auth = AuthFilter.findLegacyAuthValue(req).orElse(null);
    }
    // if found using either method, return header name/value
    if (auth != null) {
      return Optional.of(new TwoTuple<>(RequestKeys.AUTH_HEADER_LEGACY, auth));
    }

    // no auth submitted
    return Optional.empty();
  }

}
