package org.veupathdb.lib.container.jaxrs.server.middleware;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import org.gusdb.fgputil.web.LoginCookieFactory;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.repo.UserRepo;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.utils.db.DbManager;

import java.util.Optional;

/**
 * Provides client authentication checks for resource classes or methods
 * annotated with @Authenticated.
 * <p>
 * Authentication is performed by extracting the components of the
 * <code>Auth-Key</code> request header and validating those components against
 * the user database.
 *
 * @see Authenticated
 * @see OAuthAuthFilter
 *
 * @deprecated For eventual removal as we shift to oauth for all services.
 */
@Provider
@Priority(4)
@Deprecated(since = "8.0.0", forRemoval = true)
public class LegacyEnabledAuthFilter extends OAuthAuthFilter implements ContainerRequestFilter {

  public LegacyEnabledAuthFilter(Options opts) {
    super(opts);

    if (!DbManager.hasAccountDatabase())
      throw new InvalidConfigException("Account DB must be enabled to use legacy auth");

    if (anyIsEmpty(opts.getAuthSecretKey()))
      throw new InvalidConfigException("Auth secret key is required for this service");
  }

  @Override
  protected Optional<User> findAuthUser(ContainerRequestContext req) {
    return super.findAuthUser(req)
      // fall back to legacy auth (WDK cookie value or guest ID
      .or(() -> findUserFromLegacyAuth(req));
  }

  private Optional<User> findUserFromLegacyAuth(ContainerRequestContext req) {
    // find submitted auth value
    final var rawAuthOpt = findSubmittedValue(req, RequestKeys.AUTH_HEADER_LEGACY);

    // value must be non-null and non-empty
    if (rawAuthOpt.isEmpty()) {
      return Optional.empty();
    }

    final var rawAuth = rawAuthOpt.get();

    // Check if this is a guest login (Auth-Key will be just a guest user ID).
    try {
      var userID = Long.parseLong(rawAuth);
      logger.debug("Auth token is an int value.");

      // try to find registered user for this ID; if found then this is not a guest ID
      final var optUser = UserRepo.Select.registeredUserById(userID);

      // if found a registered user with this ID, then disallow access as a guest; registered users must use WDK cookie
      if (optUser.isPresent()) {
        logger.debug("Auth token is an int value but is not a guest user ID.");
        throw err401Unauthorized(null);
      }

      // guest token is not a registered user; assume valid for now (slight security hole but low-risk)
      logger.debug("Request authenticated as guest");
      req.setProperty(RequestKeys.AUTH_HEADER_LEGACY, rawAuth);
      return Optional.of((User) new User.BasicUser(userID, true, null, null).setFirstName("Guest"));
    } catch (NumberFormatException e) {
      // fall through to try to find registered user matching this auth value
      logger.debug("Auth token is not a user id.");
    } catch (Exception e) {
      throw err500Internal("failed to lookup user in user db", e);
    }

    // Auth-Key is not a guest user ID.

    LoginCookieFactory.LoginCookieParts parts;
    try {
      parts = LoginCookieFactory.parseCookieValue(rawAuth);
    } catch (IllegalArgumentException e) {
      logger.debug("Authentication failed: bad header");
      throw err401Unauthorized(null);
    }

    if (!new LoginCookieFactory(serviceOptions.getAuthSecretKey().orElseThrow()).isValidCookie(parts)) {
      logger.debug("Authentication failed: bad header");
      throw err401Unauthorized(null);
    }

    try {
      final var profile = UserRepo.Select.registeredUserByEmail(parts.getUsername());
      if (profile.isEmpty()) {
        logger.debug("Authentication failed: no such user");
        throw err401Unauthorized(null);
      }

      logger.debug("Request authenticated as a registered user");
      req.setProperty(RequestKeys.AUTH_HEADER_LEGACY, rawAuth);

      return profile;
    } catch (Exception e) {
      throw err500Internal("failed to lookup user in account db", e);
    }
  }
}
