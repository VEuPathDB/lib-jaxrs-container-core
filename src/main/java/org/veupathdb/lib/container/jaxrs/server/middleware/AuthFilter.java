package org.veupathdb.lib.container.jaxrs.server.middleware;

import jakarta.annotation.Priority;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.web.LoginCookieFactory;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.repo.UserRepo;
import org.veupathdb.lib.container.jaxrs.server.annotations.AdminRequired;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated.AdminOverrideOption;
import org.veupathdb.lib.container.jaxrs.utils.AnnotationUtil;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
public class AuthFilter implements ContainerRequestFilter {

  private static final Logger LOG = LogProvider.logger(AuthFilter.class);

  private static final ForbiddenException FORBIDDEN = new ForbiddenException();

  private static final NotAuthorizedException NOT_AUTHORIZED =
      new NotAuthorizedException("Valid credentials must be submitted to this resource.");

  private static final BadRequestException BAD_REQUEST =
      new BadRequestException("An incorrect combination of credential types was sent.");

  private static ServerErrorException SERVER_ERROR(String activity, Exception e) {
    LOG.error("Failed during authentication, " + activity, e);
    throw new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, e);
  }

  /**
   * Cache of resource references to AuthInfo details describing the auth
   * requirements and allowances of specific resources.
   */
  private final Map<String, AuthRequirements> CACHE = new ConcurrentHashMap<>();

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

    LOG.trace("AuthFilter#filter");

    // Determine what the auth requirements and/or allowances are for the target resource
    var requirements = fetchAuthRequirements();

    // look for admin and check valid (500/403), but only if needed
    boolean hasValidAdmin = requirements.adminDiscoveryRequired && hasValidAdminAuth(req);

    // return 403 response if admin required but not present
    if (requirements.adminRequired && !hasValidAdmin)
      throw new ForbiddenException();

    // return now if no need to look up user information
    if (!requirements.userDiscoveryRequired)
      return;

    // look for user submitted using normal auth (will abort with 401 if present but invalid, 500 if error)
    Optional<User> authUser = findAuthUser(req);

    // check if admin override need not be considered
    if (!hasValidAdmin || requirements.overrideOption == AdminOverrideOption.DISALLOW) {

      // user required but not present
      if (authUser.isEmpty())
        throw NOT_AUTHORIZED;

      // non-guest user required but user is guest
      if (!requirements.guestsAllowed && authUser.orElseThrow().isGuest())
        throw new ForbiddenException();

      // set request_user user to submitted value
      authUser.ifPresent(user -> req.setProperty(Globals.REQUEST_USER, user));
    }

    // valid admin wishes to override user ID and apply a custom user ID
    else { // i.e. hasValidAdmin && override != disallow

      // find proxied user
      Optional<User> proxiedUser = findProxiedUser(req);

      // if override is only allowed with proxied user but user not present, return 400
      if (requirements.overrideOption == AdminOverrideOption.ALLOW_WITH_USER && proxiedUser.isEmpty()) {
        throw BAD_REQUEST;
      }

      // set proxied user as the "request user" (may be empty)
      proxiedUser.ifPresent(user -> req.setProperty(Globals.REQUEST_USER, user));
    }
  }

  private AuthRequirements fetchAuthRequirements() {
    var ref = resource.getResourceClass().getCanonicalName() + "#" + resource.getResourceMethod().getName();
    return CACHE.computeIfAbsent(ref, key -> new AuthRequirements(resource));
  }

  public static Optional<String> findSubmittedValue(ContainerRequestContext req, String key) {

    // user can choose to submit auth key as header or query param
    final var authHeader = req.getHeaders().getFirst(key);
    final var authParam = req.getUriInfo().getQueryParameters().getFirst(key);

    // if both are submitted, they must match (no preference for one over the other)
    if (!isNull(authHeader) && !isNull(authParam) && !authHeader.equals(authParam)) {
      LOG.debug("Authentication failed: unequal auth header and query param values.");
      throw NOT_AUTHORIZED;
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
    String configuredAdminToken = opts.getAdminAuthToken().orElseThrow(
        () -> SERVER_ERROR("admin token misconfiguration", new InvalidConfigException(
            "Service enabled auth and has admin-enabled endpoints but no admin auth token is configured.")));
    Optional<String> submittedAdminToken = findSubmittedValue(req, RequestKeys.ADMIN_TOKEN_HEADER);
    if (submittedAdminToken.isEmpty())
      return false;
    if (submittedAdminToken.get().equals(configuredAdminToken))
      return true;
    // submitted but does not equal configured value
    throw FORBIDDEN;
  }

  private Optional<User> findAuthUser(ContainerRequestContext req) {

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
      LOG.debug("Auth token is an int value.");

      // try to find registered user for this ID; if found then this is not a guest ID
      final var optUser = UserRepo.Select.registeredUserById(userID);

      // if found a registered user with this ID, then disallow access as a guest; registered users must use WDK cookie
      if (optUser.isPresent()) {
        LOG.debug("Auth token is an int value but is not a guest user ID.");
        throw NOT_AUTHORIZED;
      }

      // guest token is not a registered user; assume valid for now (slight security hole but low-risk)
      LOG.debug("Request authenticated as guest");
      req.setProperty(RequestKeys.AUTH_HEADER, rawAuth);
      return Optional.of(new User()
          .setUserID(userID)
          .setFirstName("Guest")
          .setGuest(true));
    }
    catch (NumberFormatException e) {
      // fall through to try to find registered user matching this auth value
      LOG.debug("Auth token is not a user id.");
    }
    catch (Exception e) {
      throw SERVER_ERROR("failed to lookup user in user db", e);
    }

    // Auth-Key is not a guest user ID.

    LoginCookieFactory.LoginCookieParts parts;
    try {
      parts = LoginCookieFactory.parseCookieValue(rawAuth);
    }
    catch (IllegalArgumentException e) {
      LOG.debug("Authentication failed: bad header");
      throw NOT_AUTHORIZED;
    }

    if (!new LoginCookieFactory(opts.getAuthSecretKey().orElseThrow()).isValidCookie(parts)) {
      LOG.debug("Authentication failed: bad header");
      throw NOT_AUTHORIZED;
    }

    try {
      final var profile = UserRepo.Select.registeredUserByEmail(parts.getUsername());
      if (profile.isEmpty()) {
        LOG.debug("Authentication failed: no such user");
        throw NOT_AUTHORIZED;
      }

      LOG.debug("Request authenticated as a registered user");
      req.setProperty(RequestKeys.AUTH_HEADER, rawAuth);
      return profile;

    }
    catch (Exception e) {
      throw SERVER_ERROR("Failed to lookup user in account db", e);
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
        LOG.warn("Denied (401) attempt made to proxy guest user with ID " + proxiedId);
        throw NOT_AUTHORIZED;
      }

      // found registered user with this ID
      return user;
    }
    catch (NumberFormatException e) {
      LOG.warn("Denied (401) attempt made to proxy guest user with ID " + proxiedIdOpt.get());
      throw NOT_AUTHORIZED;
    }
    catch (Exception e) {
      throw SERVER_ERROR("Failed to lookup user in account db", e);
    }
  }
}

class AuthRequirements {

  // flags indicating whether to probe submitted values
  public final boolean adminDiscoveryRequired;
  public final boolean userDiscoveryRequired;

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
    adminDiscoveryRequired = adminRequired || overrideOption != AdminOverrideOption.DISALLOW;
    guestsAllowed = authAnnotationOpt.isPresent() && authAnnotationOpt.get().allowGuests();
  }
}

