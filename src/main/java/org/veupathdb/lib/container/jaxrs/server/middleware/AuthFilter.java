package org.veupathdb.lib.container.jaxrs.server.middleware;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.web.LoginCookieFactory;

import java.lang.reflect.Method;
import java.util.*;

import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.providers.RequestIdProvider;
import org.veupathdb.lib.container.jaxrs.repo.UserRepo;
import org.veupathdb.lib.container.jaxrs.server.annotations.AllowAdminAuth;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.view.error.ServerError;
import org.veupathdb.lib.container.jaxrs.view.error.UnauthorizedError;

import static java.util.Arrays.stream;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;
import static org.gusdb.fgputil.functional.Functions.with;

/**
 * Provides client authentication checks for resource classes or methods
 * annotated with @Authenticated.
 * <p>
 * Authentication is performed by extracting the components of the
 * <code>Auth-Key</code> request header and validating those components against
 * the user database.
 *
 * @see Authenticated
 */
@Provider
@Priority(4)
public class AuthFilter implements ContainerRequestFilter
{
  private static final String
    MSG_NOT_LOGGED_IN = "Users must be logged in to access this resource.",
    MSG_SERVER_ERROR  = "Login failed due to internal server error.";

  private static final Logger log = LogProvider.logger(AuthFilter.class);

  /**
   * Cache of resource references to AuthInfo details describing the auth
   * requirements and allowances of the target resource.
   */
  private final Map <String, AuthInfo> CACHE = synchronizedMap(new HashMap<>());

  private final Options opts;

  @Context
  private ResourceInfo resource;

  public AuthFilter(Options opts) {
    this.opts = opts;

    // Only validate that the secret key is present if we actually need it.
    if (opts.getAuthSecretKey()
        .filter(secretKey -> !secretKey.isBlank())
        .isEmpty())
      throw new InvalidConfigException("Auth secret key is required for this "
        + "service");
  }

  public static Optional<String> findAuthValue(ContainerRequestContext req) {

    // user can choose to submit auth key as header or query param
    final var authHeader = req.getHeaders().getFirst(RequestKeys.AUTH_HEADER);
    final var authParam = req.getUriInfo().getQueryParameters().getFirst(RequestKeys.AUTH_HEADER);

    // if both are submitted, they must match (no preference for one over the other)
    if (!isNull(authHeader) && !isNull(authParam) && !authHeader.equals(authParam)) {
      log.debug("Authentication failed: unequal auth header and query param values.");
      req.abortWith(build401());
    }

    // distill the two values to one
    final var rawAuth = authHeader == null ? authParam : authHeader;

    // treat blank values as missing
    return isNull(rawAuth) || rawAuth.isBlank() ? Optional.empty() : Optional.of(rawAuth);
  }

  @Override
  public void filter(ContainerRequestContext req) {
    log.trace("AuthFilter#filter");

    // Determine what the auth requirements and/or allowances are for the target
    // resource.
    var authInfo = fetchAuthInfo();

    // If no authentication is required, then we can stop here and allow the
    // request to continue.
    if (authInfo.authRequirement == AuthRequirement.NotRequired)
      return;

    log.debug("Authenticating request");

    // If admin auth is allowed on the target resource and the request has a
    // valid admin auth token, allow the request to continue.
    if (authInfo.adminAuthStatus == AdminAuthStatus.Allowed && hasAdminAuth(req))
      return;

    // find submitted auth value
    final var rawAuthOpt = findAuthValue(req);

    // value must be non-null and non-empty
    if (rawAuthOpt.isEmpty()) {
      log.debug("Authentication failed: no auth header or query param.");
      req.abortWith(build401());
      return;
    }

    final var rawAuth = rawAuthOpt.get();

    // Check if this is a guest login (Auth-Key will be just a guest user ID).
    if (authInfo.authRequirement == AuthRequirement.RequiredAllowGuests) {
      try {
        log.debug("Endpoint allows guest logins.");
        var userID = Long.parseLong(rawAuth);

        log.debug("Auth token is an int value.");

        // try to find registered user for this ID; if found then this is not a guest ID
        final var optUser = UserRepo.Select.registeredUserById(userID);

        // if found a registered user with this ID, then disallow access as a guest; registered users must use WDK cookie
        if (optUser.isPresent()) {
          log.debug("Auth token is an int value but is not a guest user ID.");
          req.abortWith(build401());
          return;
        }

        // guest token is not a registered user; assume valid for now (slight security hole but low-risk)
        log.debug("Request authenticated as guest");
        req.setProperty(Globals.REQUEST_USER, new User()
            .setUserID(userID)
            .setFirstName("Guest")
            .setGuest(true));
        req.setProperty(RequestKeys.AUTH_HEADER, rawAuth);
        return;
      }
      catch (NumberFormatException e) {
        // fall through to try to find registered user matching this auth value
        log.debug("Auth token is not a user id.");
      }
      catch (Exception e) {
        log.error("Failed to lookup user in user db", e);
        req.abortWith(build500(req));
      }
    }

    // Auth-Key is not a guest user ID.

    LoginCookieFactory.LoginCookieParts parts;
    try {
      parts = LoginCookieFactory.parseCookieValue(rawAuth);
    } catch (IllegalArgumentException e) {
      log.debug("Authentication failed: bad header");
      req.abortWith(build401());
      return;
    }

    if (!new LoginCookieFactory(opts.getAuthSecretKey().orElseThrow()).isValidCookie(parts)) {
      log.debug("Authentication failed: bad header");
      req.abortWith(build401());
      return;
    }

    try {
      final var profile = UserRepo.Select.registeredUserByEmail(parts.getUsername());
      if (profile.isEmpty()) {
        log.debug("Authentication failed: no such user");
        req.abortWith(build401());
        return;
      }

      log.debug("Request authenticated as a registered user");
      req.setProperty(Globals.REQUEST_USER, profile.get());
      req.setProperty(RequestKeys.AUTH_HEADER, rawAuth);

    } catch (Exception e) {
      log.error("Failed to lookup user in account db", e);
      req.abortWith(build500(req));
    }
  }

  /**
   * Helper function to build an UnauthorizedError type.
   */
  static Response build401() {
    return Response.status(Response.Status.UNAUTHORIZED)
      .entity(new UnauthorizedError(MSG_NOT_LOGGED_IN))
      .build();
  }
  static Response build500(ContainerRequestContext ctx) {
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
      .entity(new ServerError(
        RequestIdProvider.getRequestId(ctx.getRequest()),
        MSG_SERVER_ERROR
      ))
      .build();
  }

  private AuthInfo fetchAuthInfo() {
    var ref = resource.getResourceClass().getCanonicalName() + "#" + resource.getResourceMethod().getName();

    if (CACHE.containsKey(ref)) {
      return warnForAdminAuthMisconfiguration(CACHE.get(ref));
    }

    var info = new AuthInfo(determineAdminAuthStatus(), determineAuthRequirement());

    CACHE.put(ref, info);

    return warnForAdminAuthMisconfiguration(info);
  }

  private AdminAuthStatus determineAdminAuthStatus() {
    // Test whether the class or method has the AllowAdminAuth annotation.
    var hasAnnotation = stream(resource.getResourceClass().getDeclaredAnnotations())
      .anyMatch(a -> a instanceof AllowAdminAuth)
      || stream(resource.getResourceMethod().getDeclaredAnnotations())
      .anyMatch(a -> a instanceof AllowAdminAuth);

    // Test whether the service has an admin auth token configured.
    var hasAuthToken = with(opts.getAdminAuthToken(), opt -> opt.isPresent() && !opt.get().isBlank());

    return hasAnnotation
      ? (hasAuthToken ? AdminAuthStatus.Allowed : AdminAuthStatus.Misconfigured)
      : AdminAuthStatus.Disallowed;
  }

  private AuthRequirement determineAuthRequirement() {
    var opt = resourceAuthAnnotation(resource);

    if (opt.isEmpty())
      return AuthRequirement.NotRequired;

    var ann = opt.get();

    return ann.allowGuests()
      ? AuthRequirement.RequiredAllowGuests
      : AuthRequirement.RequiredDisallowGuests;
  }

  /**
   * Tests whether the given request has an admin auth header with a value that
   * matches the service's configured admin auth token value.
   *
   * @param req Request to test for a valid admin auth token.
   *
   * @return Whether the given request has a valid admin auth token header.
   */
  private boolean hasAdminAuth(ContainerRequestContext req) {
    return opts.getAdminAuthToken()
      .orElseThrow()
      .equals(req.getHeaders().getFirst(RequestKeys.ADMIN_TOKEN_HEADER));
  }

  private static Optional<Authenticated> resourceAuthAnnotation(ResourceInfo res) {
    var ann = classAuthAnnotation(res.getResourceClass());
    return ann.isPresent() ? ann : methodAuthAnnotation(res.getResourceMethod());
  }

  private static Optional<Authenticated> methodAuthAnnotation(Method meth) {
    return stream(meth.getDeclaredAnnotations())
      .filter(Authenticated.class::isInstance)
      .map(Authenticated.class::cast)
      .findFirst();
  }

  private static Optional<Authenticated> classAuthAnnotation(Class <?> type) {
    return stream(type.getDeclaredAnnotations())
      .filter(Authenticated.class::isInstance)
      .map(Authenticated.class::cast)
      .findFirst();
  }

  /**
   * Pass-through for the given AuthInfo that logs an error for the service
   * being misconfigured due to a missing admin auth token value.
   * <p>
   * This warning should appear on every request to the misconfigured service,
   * so when a developer notices admin auth not working, they will be able to
   * check the logs and see the reason.
   *
   * @param info AuthInfo to pass through.
   *
   * @return The given AuthInfo.
   */
  private static AuthInfo warnForAdminAuthMisconfiguration(AuthInfo info) {
    if (info.adminAuthStatus == AdminAuthStatus.Misconfigured)
      log.error("Resource is annotated with AllowAdminAuth but no admin auth token is configured on the service.");

    return info;
  }
}

class AuthInfo {
  public final AdminAuthStatus adminAuthStatus;
  public final AuthRequirement authRequirement;

  AuthInfo(AdminAuthStatus adminStatus, AuthRequirement authRequirement) {
    this.adminAuthStatus = adminStatus;
    this.authRequirement = authRequirement;
  }
}

enum AdminAuthStatus {
  Allowed,
  Misconfigured,
  Disallowed
}

enum AuthRequirement {
  RequiredAllowGuests,
  RequiredDisallowGuests,
  NotRequired
}
