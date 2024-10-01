package org.veupathdb.lib.container.jaxrs.providers;

import jakarta.ws.rs.core.HttpHeaders;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.oauth2.client.veupathdb.OAuthQuerier;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.model.User;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
public class UserProvider {

  public static Optional<User> lookupUser(ContainerRequest req) {
    return findRequestProp(req, User.class, Globals.REQUEST_USER);
  }

  public static Optional<TwoTuple<String, String>> getSubmittedAuth(ContainerRequest req) {
    // look for bearer token on request first
    return findRequestProp(req, String.class, HttpHeaders.AUTHORIZATION)
      // convert to submittable pair
      .map(token -> new TwoTuple<>(HttpHeaders.AUTHORIZATION, "Bearer " + token))

      // Allow for services that don't use auth themselves to fetch auth headers
      // to forward with requests made by the consuming service to other services
      // which may perform auth checks themselves.
      //
      // FIXME: this only checks for an auth header, but auth may also be
      //        provided via other means!
      .or(() -> Optional.ofNullable(req.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION))
        .map(tokenHeaderValue -> new TwoTuple<>(HttpHeaders.AUTHORIZATION, tokenHeaderValue)));
  }

  private static <T> Optional<T> findRequestProp(ContainerRequest req, Class<T> clazz, String key) {
    return Optional.ofNullable(Objects.requireNonNull(req).getProperty(key)).map(clazz::cast);
  }

  public static Map<Long,User> getUsersById(List<Long> userIds) {
    return OAuthQuerier.getUsersById(OAuthProvider.getOAuthClient(), OAuthProvider.getOAuthConfig(), userIds, User.BasicUser::new);
  }

  public static Map<String,User> getUsersByEmail(List<String> emails) {
    return OAuthQuerier.getUsersByEmail(OAuthProvider.getOAuthClient(), OAuthProvider.getOAuthConfig(), emails, User.BasicUser::new);
  }
}
