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
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
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
    MSG_SERVER_ERROR  = "Login failed due to internal server error.";

  private static final Logger log = LogProvider.logger(AuthFilter.class);

  /**
   * Cache of resource classes or methods and whether or not they require
   * authentication.  This is to prevent repeatedly reflectively searching
   * the types for the {@link Authenticated} annotation.
   */
  private final Map < String, AuthRequirement > CACHE = synchronizedMap(new HashMap <>());

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

    var authRequirement = authRequirement(resource);
    if (authRequirement == AuthRequirement.NotRequired)
      return;

    log.debug("Authenticating request");

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
    if (authRequirement == AuthRequirement.RequiredAllowGuests) {
      try {
        log.debug("Endpoint allows guest logins.");
        var userID = Long.parseLong(rawAuth);

        log.debug("Auth token is an int value.");

        var optUser = UserRepo.Select.guestUserByID(userID);

        // We matched a user and that user is a guest.
        if (optUser.isPresent()) {
          log.debug("Request authenticated as guest");
          req.setProperty(Globals.REQUEST_USER, optUser.get());
          req.setProperty(RequestKeys.AUTH_HEADER, rawAuth);
          return;
        }

        // If we made it this far we know that the auth token passed is an int
        // value which means it can't be a login cookie, so we can bail early.
        log.debug("Auth token is an int value but is not a guest user ID.");
        req.abortWith(build401());
        return;
      } catch (NumberFormatException e) {
        log.debug("Auth token is not a user id.");
      } catch (Exception e) {
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

  enum AuthRequirement {
    RequiredAllowGuests,
    RequiredDisallowGuests,
    NotRequired
  }

  /**
   * Checks if the given resource is annotated with the {@link Authenticated}
   * annotation.
   *
   * @param res resource to check.
   *
   * @return whether or not the resource has the auth annotation.
   */
  AuthRequirement authRequirement(ResourceInfo res) {
    log.trace("AuthFilter#isAuthRequired");

    final var meth = res.getResourceMethod();
    final var type = res.getResourceClass();

    // If we have a value cached for the type, return that cached value.
    final var typeName = type.getName(); {
      var cached = CACHE.get(typeName);
      if (cached != null) {
        return cached;
      }
    }

    // If we have a value cached for the endpoint method, return that cached
    // value.
    final var methName = typeName + '#' + meth.getName(); {
      var cached = CACHE.get(methName);
      if (cached != null) {
        return cached;
      }
    }

    var methodAuth = methodAuthAnnotation(meth);
    if (methodAuth.isPresent()) {
      var authType = methodAuth.get().allowGuests()
        ? AuthRequirement.RequiredAllowGuests
        : AuthRequirement.RequiredDisallowGuests;

      CACHE.put(methName, authType);
      return authType;
    } else {
      CACHE.put(methName, AuthRequirement.NotRequired);
    }

    var classAuth = classAuthAnnotation(type);
    if (classAuth.isPresent()) {
      var authType = classAuth.get().allowGuests()
        ? AuthRequirement.RequiredAllowGuests
        : AuthRequirement.RequiredDisallowGuests;

      CACHE.put(typeName, authType);
      return authType;
    } else {
      CACHE.put(typeName, AuthRequirement.NotRequired);
    }

    return AuthRequirement.NotRequired;
  }

  /**
   * Reflectively checks whether or not the give method is annotated with
   * {@link Authenticated}.
   *
   * @param meth Method to check
   *
   * @return whether or not the methods has the auth annotation.
   */
  Optional<Authenticated> methodAuthAnnotation(Method meth) {
    log.trace("AuthFilter#methodHasAuth");
    return Arrays.stream(meth.getDeclaredAnnotations())
      .filter(Authenticated.class::isInstance)
      .map(Authenticated.class::cast)
      .findFirst();
  }

  /**
   * Reflectively checks whether or not the given class is annotated with
   * {@link Authenticated}.
   *
   * @param type Class to check
   *
   * @return whether or not the class has the auth annotation.
   */
  Optional<Authenticated> classAuthAnnotation(Class < ? > type) {
    log.trace("AuthFilter#classHasAuth");
    return Arrays.stream(type.getDeclaredAnnotations())
      .filter(Authenticated.class::isInstance)
      .map(Authenticated.class::cast)
      .findFirst();
  }
}
