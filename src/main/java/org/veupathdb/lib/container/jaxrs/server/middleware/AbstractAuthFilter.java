package org.veupathdb.lib.container.jaxrs.server.middleware;

import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.repo.UserRepo;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;

abstract class AbstractAuthFilter implements ContainerRequestFilter {
  protected final Logger logger = LogProvider.logger(this.getClass());

  /**
   * Cache of resource references to AuthInfo details describing the auth
   * requirements and allowances of specific resources.
   */
  protected final Map<String, AuthRequirements> authRequirementsCache = new ConcurrentHashMap<>(32);

  protected final Options serviceOptions;

  @Context
  protected ResourceInfo resource;

  AbstractAuthFilter(Options options) {
    serviceOptions = options;
  }

  @Override
  public void filter(ContainerRequestContext req) {
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
    if (!hasValidAdmin || requirements.overrideOption == Authenticated.AdminOverrideOption.DISALLOW) {

      // user required but not present
      if (authUser.isEmpty())
        throw err401Unauthorized(null);

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
      if (requirements.overrideOption == Authenticated.AdminOverrideOption.ALLOW_WITH_USER && proxiedUser.isEmpty()) {
        throw err400BadRequest(null);
      }

      // set proxied user as the "request user" (may be empty)
      proxiedUser.ifPresent(user -> req.setProperty(Globals.REQUEST_USER, user));
    }
  }

  protected abstract Optional<User> findAuthUser(ContainerRequestContext req);

  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃    Exception Type Constructors                     ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/

  protected WebApplicationException err400BadRequest(String message) {
    if (message == null || message.isBlank())
      return new BadRequestException("Valid credentials must be submitted to this resource.");
    else
      return new BadRequestException(message);
  }

  protected WebApplicationException err401Unauthorized(String message) {
    if (message == null || message.isBlank())
      return new NotAuthorizedException("Valid credentials must be submitted to this resource.");
    else
      return new NotAuthorizedException(message);
  }

  protected WebApplicationException err403Forbidden(String message) {
    if (message == null || message.isBlank())
      return new ForbiddenException();
    else
      return new ForbiddenException(message);
  }

  protected WebApplicationException err500Internal(String activity, Exception cause) {
    //noinspection StringConcatenationArgumentToLogCall
    logger.error("failed during authentication: " + activity, cause);
    return new ServerErrorException(Response.Status.INTERNAL_SERVER_ERROR, cause);
  }


  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃    Shared Internal API                             ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/


  protected Optional<String> findSubmittedValue(ContainerRequestContext req, String key) {
    // user can choose to submit auth values as header or query param
    return resolveSingleValue(req.getHeaders().getFirst(key), req.getUriInfo().getQueryParameters().getFirst(key), null);
  }

  protected Optional<String> resolveSingleValue(
    String headerValue,
    String paramValue,
    String cookieValue
  ) {
    // if both are submitted, they must match (no preference for one over the other)
    if (!isNull(headerValue) && !isNull(paramValue) && !headerValue.equals(paramValue)) {
      logger.debug("Authentication failed: unequal auth header and query param values.");
      throw err400BadRequest(null);
    }

    // distill the three values to one (cookie last, only if other two are null)
    final var submittedValue = (headerValue != null) ? headerValue : ((paramValue != null) ? paramValue : cookieValue);

    // treat blank values as missing
    return isNull(submittedValue) || submittedValue.isBlank() ? Optional.empty() : Optional.of(submittedValue);
  }

  @SafeVarargs
  protected static boolean anyIsEmpty(Optional<String>... opts) {
    for (var opt : opts) {
      if (opt.isEmpty() || opt.get().isBlank())
        return true;
    }

    return false;
  }


  /*┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓*\
    ┃    Private Internal API                            ┃
  \*┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛*/


  private Optional<User> findProxiedUser(ContainerRequestContext req) {
    // find submitted value
    final var proxiedIdOpt = findSubmittedValue(req, RequestKeys.PROXIED_USER_ID_HEADER);

    // value must be non-null and non-empty
    if (proxiedIdOpt.isEmpty()) {
      return Optional.empty();
    }

    long proxiedId;
    try {
      proxiedId = Long.parseLong(proxiedIdOpt.get());
    } catch (NumberFormatException e) {
      logger.warn("Denied (401) attempt made to proxy guest user with invalid or non-numeric ID");
      throw err401Unauthorized(null);
    }

    // try to find registered user for this ID; guests can not be proxied
    Optional<User> user;
    try {
      user = UserRepo.Select.registeredUserById(proxiedId);
    } catch (Exception e) {
      throw err500Internal("Failed to lookup user in account db", e);
    }

    if (user.isEmpty()) {
      logger.warn("Denied (401) attempt made to proxy guest user with ID {}", proxiedId);
      throw err401Unauthorized(null);
    }

    return user;
  }

  private AuthRequirements fetchAuthRequirements() {
    var ref = resource.getResourceClass().getCanonicalName() + "#" + resource.getResourceMethod().getName();
    return authRequirementsCache.computeIfAbsent(ref, key -> new AuthRequirements(resource));
  }

  /**
   * Tests whether the given request has an admin auth header with a value that
   * matches the service's configured admin auth token value.  Returns false if
   * no admin token is on the request.  Will abort with 403 if an admin token was
   * submitted but does not match the configured value.  Will abort with 500 if
   * admin token was not configured.
   *
   * @param req Request to test for a valid admin auth token
   *
   * @return Whether the given request has a valid admin auth token header
   */
  private boolean hasValidAdminAuth(ContainerRequestContext req) {
    String configuredAdminToken = serviceOptions.getAdminAuthToken()
      .orElseThrow(() -> err500Internal(
        "admin token misconfiguration",
        new InvalidConfigException("Service enabled auth and has admin-enabled endpoints but no admin auth token is configured.")
      ));

    Optional<String> submittedAdminToken = findSubmittedValue(req, RequestKeys.ADMIN_TOKEN_HEADER);

    if (submittedAdminToken.isEmpty())
      return false;

    if (submittedAdminToken.get().equals(configuredAdminToken))
      return true;

    // submitted but does not equal configured value
    throw err403Forbidden(null);
  }
}
