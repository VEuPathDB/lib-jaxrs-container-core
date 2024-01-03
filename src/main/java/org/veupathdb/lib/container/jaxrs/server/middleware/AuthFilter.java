package org.veupathdb.lib.container.jaxrs.server.middleware;

import jakarta.annotation.Priority;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.web.LoginCookieFactory;

import java.util.*;

import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.providers.RequestIdProvider;
import org.veupathdb.lib.container.jaxrs.repo.UserRepo;
import org.veupathdb.lib.container.jaxrs.server.annotations.AdminRequired;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated.AdminOverrideOption;
import org.veupathdb.lib.container.jaxrs.utils.AnnotationUtil;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.view.error.ServerError;
import org.veupathdb.lib.container.jaxrs.view.error.UnauthorizedError;

import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;

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
    MSG_FORBIDDEN = "Access disallowed with submitted credentials.",
    MSG_SERVER_ERROR  = "Login failed due to internal server error.";

  private static final Logger log = LogProvider.logger(AuthFilter.class);

  /**
   * Cache of resource references to AuthInfo details describing the auth
   * requirements and allowances of specific resources.
   */
  private final Map <String, AuthRequirements> CACHE = synchronizedMap(new HashMap<>());

  private final Options opts;

  @Context
  private ResourceInfo resource;

  public AuthFilter(Options opts) {
    this.opts = opts;

    // Only validate that the secret key is present if we actually need it.
    if (opts.getAuthSecretKey()
        .filter(secretKey -> !secretKey.isBlank())
        .isEmpty())
      throw new InvalidConfigException("Auth secret key is required for this service");
  }

  @Override
  public void filter(ContainerRequestContext req) {
    log.trace("AuthFilter#filter");

    // Determine what the auth requirements and/or allowances are for the target resource
    var requirements = fetchAuthRequirements();

    boolean hasValidAdmin = false;

    // only check if needed
    if (requirements.adminDiscoveryRequired) {
      //look for admin and check valid (500/403);
      hasValidAdmin = hasValidAdminAuth(req);
    }

    if (requirements.adminRequired && !hasValidAdmin) {
      req.abortWith(build403());
    }

    if (!requirements.userDiscoveryRequired) return; // done

    // look for user in Auth-Key (500/401)
    Optional<User> authKeyUser = findUser(req);

    // check if override is not needed
    if (requirements.overrideOption == AdminOverrideOption.DISALLOW || !hasValidAdmin) {

      // user required but not present
      if (authKeyUser.isEmpty())
        req.abortWith(build401());

      // non-guest user required but user is guest
      if (!requirements.guestsAllowed && authKeyUser.orElseThrow().isGuest())
        req.abortWith(build403());

      // set request user to submitted value
      req.setProperty(Globals.REQUEST_USER, authKeyUser);
      return;
    }

    // reaching here means ( override != disallow && hasValidAdmin )
    // find proxied user
    Optional<User> proxiedUser = findProxiedUser(req);

    // if override only allowed with proxied user but user not present, throw 401
    if (requirements.overrideOption == AdminOverrideOption.ALLOW_WITH_USER && proxiedUser.isEmpty())
      req.abortWith(build401());

    // set proxied user as the "request user"
    req.setProperty(Globals.REQUEST_USER, authKeyUser);

  }

  private AuthRequirements fetchAuthRequirements() {
    var ref = resource.getResourceClass().getCanonicalName() + "#" + resource.getResourceMethod().getName();
    if (!CACHE.containsKey(ref)) {
      CACHE.put(ref, new AuthRequirements(resource));
    }
    return CACHE.get(ref);
  }

  public static Optional<String> findSubmittedValue(ContainerRequestContext req, String key) {

    // user can choose to submit auth key as header or query param
    final var authHeader = req.getHeaders().getFirst(key);
    final var authParam = req.getUriInfo().getQueryParameters().getFirst(key);

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

  /**
   * Tests whether the given request has an admin auth header with a value that
   * matches the service's configured admin auth token value.  Returns false if
   * no admin token is on the request.  Will abort with 403 if an admin token was
   * submitted but does not match the configured value.  Will abort with 500 if
   * admin token was not configured.
   *
   * @param req Request to test for a valid admin auth token
   * @return Whether the given request has a valid admin auth token header
   */
  private boolean hasValidAdminAuth(ContainerRequestContext req) {
    String configuredAdminToken = opts.getAdminAuthToken().orElseGet(() -> {
        req.abortWith(build500(req, new InvalidConfigException(
            "Service enabled auth and has admin-enabled endpoints but no admin auth token is configured.")));
        return null;
    });
    Optional<String> submittedAdminToken = findSubmittedValue(req, RequestKeys.ADMIN_TOKEN_HEADER);
    if (submittedAdminToken.isEmpty())
      return false;
    if (submittedAdminToken.get().equals(configuredAdminToken))
      return true;
    // submitted but does not equal configured value
    req.abortWith(build403());
    return false;
  }

  private Optional<User> findUser(ContainerRequestContext req) {

    // find submitted auth value
    final var rawAuthOpt = findSubmittedValue(req, RequestKeys.AUTH_HEADER);

    // value must be non-null and non-empty
    if (rawAuthOpt.isEmpty()) {
      return Optional.empty();
    }

    final var rawAuth = rawAuthOpt.get();

    // Check if this is a guest login (Auth-Key will be just a guest user ID).
    try {
      var userID = Long.parseLong(rawAuth);
      log.debug("Auth token is an int value.");

      // try to find registered user for this ID; if found then this is not a guest ID
      final var optUser = UserRepo.Select.registeredUserById(userID);

      // if found a registered user with this ID, then disallow access as a guest; registered users must use WDK cookie
      if (optUser.isPresent()) {
        log.debug("Auth token is an int value but is not a guest user ID.");
        req.abortWith(build401());
        return Optional.empty(); // irrelevant due to abort
      }

      // guest token is not a registered user; assume valid for now (slight security hole but low-risk)
      log.debug("Request authenticated as guest");
      req.setProperty(RequestKeys.AUTH_HEADER, rawAuth);
      return Optional.of(new User()
          .setUserID(userID)
          .setFirstName("Guest")
          .setGuest(true));
    }
    catch (NumberFormatException e) {
      // fall through to try to find registered user matching this auth value
      log.debug("Auth token is not a user id.");
    }
    catch (Exception e) {
      log.error("Failed to lookup user in user db", e);
      req.abortWith(build500(req, e));
    }

    // Auth-Key is not a guest user ID.

    LoginCookieFactory.LoginCookieParts parts;
    try {
      parts = LoginCookieFactory.parseCookieValue(rawAuth);
    } catch (IllegalArgumentException e) {
      log.debug("Authentication failed: bad header");
      req.abortWith(build401());
      return Optional.empty(); // irrelevant due to abort
    }

    if (!new LoginCookieFactory(opts.getAuthSecretKey().orElseThrow()).isValidCookie(parts)) {
      log.debug("Authentication failed: bad header");
      req.abortWith(build401());
      return Optional.empty(); // irrelevant due to abort
    }

    try {
      final var profile = UserRepo.Select.registeredUserByEmail(parts.getUsername());
      if (profile.isEmpty()) {
        log.debug("Authentication failed: no such user");
        req.abortWith(build401());
        return Optional.empty(); // irrelevant due to abort
      }

      log.debug("Request authenticated as a registered user");
      req.setProperty(RequestKeys.AUTH_HEADER, rawAuth);
      return profile;

    } catch (Exception e) {
      log.error("Failed to lookup user in account db", e);
      req.abortWith(build500(req, e));
      return Optional.empty(); // irrelevant due to abort
    }
  }

  private Optional<User> findProxiedUser(ContainerRequestContext req) {

    // find submitted value
    final var proxiedIdOpt = findSubmittedValue(req, RequestKeys.PROXIED_USER_ID);

    // value must be non-null and non-empty
    if (proxiedIdOpt.isEmpty()) {
      return Optional.empty();
    }

    try {
      var proxiedId = Long.parseLong(proxiedIdOpt.get());

      // try to find registered user for this ID; guests can not be proxied
      Optional<User> user = UserRepo.Select.registeredUserById(proxiedId);

      if (user.isEmpty()) {
        log.warn("Denied (401) attempt made to proxy guest user with ID " + proxiedId);
        req.abortWith(build401());
        return Optional.empty(); // irrelevant due to abort
      }

      // found registered user with this ID
      return user;
    }
    catch (NumberFormatException e) {
      log.warn("Denied (401) attempt made to proxy guest user with ID " + proxiedIdOpt.get());
      req.abortWith(build401());
      return Optional.empty(); // irrelevant due to abort
    }
    catch (Exception e) {
      log.error("Failed to lookup user in account db", e);
      req.abortWith(build500(req, e));
      return Optional.empty(); // irrelevant due to abort
    }
  }

  /*
   * Helper functions to build error responses
   */
  static Response build401() {
    return Response.status(Response.Status.UNAUTHORIZED)
        .entity(new UnauthorizedError(MSG_NOT_LOGGED_IN))
        .build();
  }
  static Response build403() {
    return Response.status(Response.Status.FORBIDDEN)
        .entity(new ForbiddenException(MSG_FORBIDDEN))
        .build();
  }
  static Response build500(ContainerRequestContext ctx, Exception e) {
    log.error("Failed during authentication", e);
    return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
        .entity(new ServerError(
            RequestIdProvider.getRequestId(ctx.getRequest()),
            MSG_SERVER_ERROR
        ))
        .build();
  }
}

class AuthRequirements {

  // flags indicating whether to probe submitted values
  public final boolean adminDiscoveryRequired;
  public final boolean userDiscoveryRequired;
  public final boolean proxiedUserDiscoveryRequired;

  // flags indicating how to handle submitted values
  public final boolean adminRequired;
  public final boolean guestsAllowed;
  public final AdminOverrideOption overrideOption;

  AuthRequirements(ResourceInfo resource) {
    Optional<Authenticated> authAnnotationOpt = AnnotationUtil.findResourceAnnotation(resource, Authenticated.class);
    Optional<AdminRequired> adminAnnotationOpt = AnnotationUtil.findResourceAnnotation(resource, AdminRequired.class);
    adminRequired = adminAnnotationOpt.isPresent();
    userDiscoveryRequired = authAnnotationOpt.isPresent();
    overrideOption = authAnnotationOpt.map(Authenticated::adminOverride).orElse(AdminOverrideOption.DISALLOW);
    boolean adminOverrideDisallowed = overrideOption != AdminOverrideOption.DISALLOW;
    adminDiscoveryRequired = adminRequired || !adminOverrideDisallowed;
    proxiedUserDiscoveryRequired = !adminOverrideDisallowed;
    guestsAllowed = authAnnotationOpt.isPresent() && authAnnotationOpt.get().allowGuests();
  }
}

