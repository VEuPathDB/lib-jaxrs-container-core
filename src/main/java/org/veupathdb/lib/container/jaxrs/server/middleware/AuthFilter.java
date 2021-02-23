package org.veupathdb.lib.container.jaxrs.server.middleware;

import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.web.LoginCookieFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import java.lang.reflect.Method;
import java.util.*;

import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.model.User;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
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

  private final Logger log = LogProvider.logger(AuthFilter.class);

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
    if (opts.getAuthSecretKey().isEmpty())
      throw new InvalidConfigException("Auth secret key is required for this "
        + "service");
  }

  @Override
  public void filter(ContainerRequestContext req) {
    log.trace("AuthFilter#filter");

    var authRequirement = authRequirement(resource);
    if (authRequirement == AuthRequirement.NotRequired)
      return;

    log.debug("Authenticating request");

    final var rawAuth = req.getHeaders().getFirst(RequestKeys.AUTH_HEADER);

    if (isNull(rawAuth) || rawAuth.isEmpty()) {
      log.debug("Authentication failed: no auth header.");
      req.abortWith(build401());
      return;
    }

    // Check if this is a guest login (Auth-Key will be just a guest user ID).
    if (authRequirement == AuthRequirement.RequiredAllowGuests) {
      try {
        log.debug("Endpoint allows guest logins.");
        var userID = Long.parseLong(rawAuth);

        log.debug("Auth token is an int value.");
        var optUser = UserRepo.Select.userByID(userID);

        var user = optUser.orElseGet(() -> new User().setFirstName("Guest").setUserID(userID));
        if (optUser.isPresent()) {

          UserRepo.Select.populateIsGuest(user);

          // We matched a user and that user is a guest.
          if (user.isGuest()) {
            log.debug("Request authenticated as guest");
            req.setProperty(Globals.REQUEST_USER, user);
            return;
          }
        }

        // If we made it this far we know that the auth token passed is an int
        // value which means it can't be a login cookie, so we can bail early.
        log.debug("Auth token is an int value but is not a guest user ID.");
        req.abortWith(build401());
        return;
      } catch (NumberFormatException e) {
        log.debug("Auth token is not a user id.");
      } catch (Exception e) {
        log.error("Failed to lookup user in account db", e);
        req.abortWith(build500());
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
      final var profile = UserRepo.Select.userByUsername(parts.getUsername());
      if (isNull(profile)) {
        log.debug("Authentication failed: no such user");
        req.abortWith(build401());
        return;
      }

      log.debug("Request authenticated");
      req.setProperty(Globals.REQUEST_USER, profile);
    } catch (Exception e) {
      log.error("Failed to lookup user in account db", e);
      req.abortWith(build500());
    }
  }

  /**
   * Helper function to build an UnauthorizedError type.
   */
  static Response build401() {
    return Response.status(Status.UNAUTHORIZED)
      .entity(new UnauthorizedError(MSG_NOT_LOGGED_IN))
      .build();
  }
  static Response build500() {
    return Response.status(Status.INTERNAL_SERVER_ERROR)
      .entity(new ServerError(MSG_SERVER_ERROR))
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
