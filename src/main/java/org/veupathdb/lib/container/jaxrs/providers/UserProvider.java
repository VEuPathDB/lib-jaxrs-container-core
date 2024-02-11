package org.veupathdb.lib.container.jaxrs.providers;

import jakarta.ws.rs.core.HttpHeaders;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.oauth2.client.veupathdb.OAuthQuerier;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class UserProvider {

  public static Optional<User> lookupUser(ContainerRequest req) {
    return findRequestProp(req, User.class, Globals.REQUEST_USER);
  }

  public static Optional<TwoTuple<String, String>> getSubmittedAuth(ContainerRequest req) {
    return
      // look for bearer token on request first
      findRequestProp(req, String.class, HttpHeaders.AUTHORIZATION)
        // convert to submittable pair
        .map(token -> new TwoTuple<>(HttpHeaders.AUTHORIZATION, "Bearer " + token))
    .or(() ->
      // look for legacy auth key (i.e. WDK cookie value or guest ID)
      findRequestProp(req, String.class, RequestKeys.AUTH_HEADER_LEGACY)
        // convert to submittable pair
        .map(token -> new TwoTuple<>(RequestKeys.AUTH_HEADER_LEGACY, token)));
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
