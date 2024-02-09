package org.veupathdb.lib.container.jaxrs.providers;

import jakarta.ws.rs.core.HttpHeaders;
import org.glassfish.jersey.server.ContainerRequest;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.OAuthConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

import java.util.*;
import java.util.stream.Collectors;

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

  public static Map<Long,User> getUserData(List<Long> userIds) {
    OAuthClient client = OAuthProvider.getOAuthClient();
    OAuthConfig config = OAuthProvider.getOAuthConfig();
    List<String> idStrings = userIds.stream().map(String::valueOf).collect(Collectors.toList());
    JSONArray json = client.getUserData(config, idStrings);
    Map<Long,User> users = new LinkedHashMap<>();
    for (int i = 0; i < json.length(); i++) {
      JSONObject userJson = json.getJSONObject(i);
      User user = new User.BasicUser(userJson);
      users.put(user.getUserId(), user);
    }
    return users;
  }

}
