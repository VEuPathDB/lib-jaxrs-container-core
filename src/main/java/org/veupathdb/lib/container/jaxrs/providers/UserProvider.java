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

@SuppressWarnings("unused")
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
        .map(token -> new TwoTuple<>(RequestKeys.AUTH_HEADER_LEGACY, token)))

     // FIXME: stop gap for EDA merge service to forward unvalidated auth header to dataset access even
     //        when auth is disabled (AuthFilter does not run).  Cannot run AuthFilter without auth_secret_key,
     //        which is not currently configured in docker-compose for EDA merging, and don't want to ask systems.
     //        NOTE: no longer have access to query params here so if auth is submitted via params, this won't work.
     // TODO: remove along with legacy auth logic once bearer tokens are ubiquitous
     .or(() -> Optional.ofNullable(req.getRequestHeaders().getFirst(HttpHeaders.AUTHORIZATION))
         .map(tokenHeaderValue -> new TwoTuple<>(HttpHeaders.AUTHORIZATION, tokenHeaderValue)))
     .or(() -> Optional.ofNullable(req.getRequestHeaders().getFirst(RequestKeys.AUTH_HEADER_LEGACY))
         .map(legacyHeaderValue -> new TwoTuple<>(RequestKeys.AUTH_HEADER_LEGACY, legacyHeaderValue)));
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
