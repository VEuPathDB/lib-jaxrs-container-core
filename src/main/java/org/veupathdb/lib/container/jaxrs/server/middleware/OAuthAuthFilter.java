package org.veupathdb.lib.container.jaxrs.server.middleware;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.oauth2.exception.ExpiredTokenException;
import org.gusdb.oauth2.exception.InvalidTokenException;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.OAuthProvider;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

import java.util.Optional;

/**
 * Provides client authentication via OAuth checks for resource classes or
 * methods annotated with {@code @Authenticated}.
 *
 * @see Authenticated
 */
@Provider
@Priority(4)
public class OAuthAuthFilter extends AbstractAuthFilter implements ContainerRequestFilter {

  public OAuthAuthFilter(Options opts) {
    super(opts);

    if (anyIsEmpty(opts.getOAuthUrl(), opts.getOAuthClientId(), opts.getOAuthClientSecret()))
      throw new InvalidConfigException("missing required OAuth configuration values");
  }

  @Override
  protected Optional<User> findAuthUser(ContainerRequestContext req) {
    // user can choose to submit authorization value as header or query param
    final var authHeader = req.getHeaders().getFirst(RequestKeys.BEARER_TOKEN_HEADER);
    final var headerToken = authHeader == null ? null : OAuthClient.getTokenFromAuthHeader(authHeader);
    final var paramToken = req.getUriInfo().getQueryParameters().getFirst(RequestKeys.BEARER_TOKEN_QUERY_PARAM);
    final var cookie = req.getCookies().get(RequestKeys.BEARER_TOKEN_HEADER);
    final var cookieToken = cookie == null ? null : cookie.getValue();
    final var bearerToken = resolveSingleValue(headerToken, paramToken, cookieToken);

    // convert bearerToken to User
    if (bearerToken.isEmpty()) return Optional.empty();

    OAuthClient client = OAuthProvider.getOAuthClient();
    String oauthUrl = OAuthProvider.getOAuthUrl();

    try {
      // parse and validate the token and its signature
      ValidatedToken token = client.getValidatedEcdsaSignedToken(oauthUrl, bearerToken.get());

      // set token on the request in case application logic needs it
      req.setProperty(RequestKeys.BEARER_TOKEN_HEADER, token.getTokenValue());

      // create new user from this token
      return Optional.of(new User.BearerTokenUser(client, oauthUrl, token));
    }
    catch (InvalidTokenException | ExpiredTokenException e) {
      logger.warn("User submitted invalid bearer token: {}", bearerToken.get());
      throw err401Unauthorized(null);
    }
  }
}
