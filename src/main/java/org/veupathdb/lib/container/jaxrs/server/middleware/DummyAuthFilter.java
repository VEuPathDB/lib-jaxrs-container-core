package org.veupathdb.lib.container.jaxrs.server.middleware;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.web.LoginCookieFactory;
import org.gusdb.oauth2.client.veupathdb.User;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.view.error.UnauthorizedError;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;

/**
 * Provides passthrough client authentication checks for resource classes or
 * methods annotated with @Authenticated.
 *
 * @see Authenticated
 */
@Provider
@Priority(4)
public class DummyAuthFilter implements ContainerRequestFilter {

  private static final String MESSAGE = "Users must be logged in to access this"
    + " resource.";

  private final Logger log = LogProvider.logger(DummyAuthFilter.class);

  /**
   * Cache of resource classes or methods and whether or not they require
   * authentication.  This is to prevent repeatedly reflectively searching
   * the types for the {@link Authenticated} annotation.
   */
  private final Map<String, Boolean> CACHE = synchronizedMap(new HashMap<>());


  @Context
  private ResourceInfo resource;

  @Override
  public void filter(ContainerRequestContext req) {
    log.trace("DummyAuthFilter#filter");

    if (!isAuthRequired(resource))
      return;

    log.debug("Authenticating request");

    final var rawAuth = req.getHeaders().getFirst(RequestKeys.AUTH_HEADER_LEGACY);

    if (isNull(rawAuth) || rawAuth.isEmpty()) {
      log.debug("Authentication failed: no auth header.");
      req.abortWith(build401());
      return;
    }

    final var auth = LoginCookieFactory.parseCookieValue(rawAuth);

    log.debug("Request authenticated");
    req.setProperty(Globals.REQUEST_USER, new User(123456L, false, null, "USER123456")
        .setEmail(auth.getUsername())
        .setFirstName("demo")
        .setLastName("user")
    );
  }

  /**
   * Helper function to build an UnauthorizedError type.
   *
   * @return A response object containing a constructed 401 error body.
   */
  static Response build401() {
    return Response.status(Status.UNAUTHORIZED)
      .entity(new UnauthorizedError(MESSAGE))
      .build();
  }

  /**
   * Checks if the given resource is annotated with the {@link Authenticated}
   * annotation.
   *
   * @param res resource to check.
   *
   * @return whether or not the resource has the auth annotation.
   */
  boolean isAuthRequired(ResourceInfo res) {
    log.trace("DummyAuthFilter#isAuthRequired");

    final var meth = res.getResourceMethod();
    final var type = res.getResourceClass();

    final var typeName = type.getName();
    final var methName = typeName + '#' + meth.getName();

    if (CACHE.getOrDefault(typeName, false))
      return true;
    else if (CACHE.containsKey(methName))
      return CACHE.get(methName);

    if (methodHasAuth(meth)) {
      CACHE.put(methName, true);
      return true;
    } else {
      CACHE.put(methName, false);
    }

    if (classHasAuth(type)) {
      CACHE.put(typeName, true);
      return true;
    } else {
      CACHE.put(typeName, false);
    }

    return false;
  }

  /**
   * Reflectively checks whether or not the give method is annotated with
   * {@link Authenticated}.
   *
   * @param meth Method to check
   *
   * @return whether or not the methods has the auth annotation.
   */
  boolean methodHasAuth(Method meth) {
    log.trace("DummyAuthFilter#methodHasAuth");
    return Arrays.stream(meth.getDeclaredAnnotations())
      .anyMatch(Authenticated.class::isInstance);
  }

  /**
   * Reflectively checks whether or not the given class is annotated with
   * {@link Authenticated}.
   *
   * @param type Class to check
   *
   * @return whether or not the class has the auth annotation.
   */
  boolean classHasAuth(Class<?> type) {
    log.trace("DummyAuthFilter#classHasAuth");
    return Arrays.stream(type.getDeclaredAnnotations())
      .anyMatch(Authenticated.class::isInstance);
  }

}
