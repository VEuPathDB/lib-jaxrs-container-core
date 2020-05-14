package org.veupathdb.lib.container.jaxrs.middleware;

import org.apache.logging.log4j.Logger;
import org.gusdb.fgputil.accountdb.AccountManager;
import org.gusdb.fgputil.accountdb.UserPropertyName;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.web.LoginCookieFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.view.error.UnauthorizedError;

import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;

/**
 * Provides client authentication checks for resource classes or methods
 * annotated with @Authenticated.
 *
 * @see Authenticated
 */
@Provider
@Priority(3)
public class AuthFilter implements ContainerRequestFilter {

  private static final String MESSAGE = "Users must be logged in to access this"
    + " resource.";

  private final Logger log = LogProvider.logger(AuthFilter.class);

  /**
   * Cache of resource classes or methods and whether or not they require
   * authentication.  This is to prevent repeatedly reflectively searching
   * the types for the {@link Authenticated} annotation.
   */
  private final Map<String, Boolean> CACHE = synchronizedMap(new HashMap<>());

  private final Options opts;

  private final AccountManager acctMan;

  @Context
  private ResourceInfo resource;

  public AuthFilter(
    Options opts,
    DatabaseInstance acctDb
  ) {
    this.opts = opts;
    this.acctMan = new AccountManager(acctDb, Globals.DB_ACCOUNT_SCHEMA, Arrays.asList(
      new UserPropertyName("firstName", "first_name", true),
      new UserPropertyName("middleName", "middle_name", true),
      new UserPropertyName("lastName", "last_name", true),
      new UserPropertyName("organization", "organization", true)));

    // Only validate that the secret key is present if we actually need it.
    if (opts.getAuthSecretKey().isEmpty())
      throw new InvalidConfigException("Auth secret key is required for this "
        + "service");
  }

  @Override
  public void filter(ContainerRequestContext req) {
    log.trace("AuthFilter#filter");

    if (!isAuthRequired(resource))
      return;

    log.debug("Authenticating request");

    final var rawAuth = req.getCookies().get(RequestKeys.AUTH_HEADER).getValue();

    if (isNull(rawAuth) || rawAuth.isEmpty()) {
      log.debug("Authentication failed: no auth cookie.");
      req.abortWith(build401());
      return;
    }

    final var auth = LoginCookieFactory.
      parseCookieValue(rawAuth);

    if (!new LoginCookieFactory(
      opts.getAuthSecretKey().orElseThrow()).isValidCookie(auth)) {
      log.debug("Authentication failed: bad cookie");
      req.abortWith(build401());
      return;
    }

    final var profile = acctMan.getUserProfile(auth.getUsername());
    if (isNull(profile)) {
      log.debug("Authentication failed: no such user");
      req.abortWith(build401());
      return;
    }

    log.debug("Request authenticated");
    req.setProperty(Globals.REQUEST_USER, profile);
  }

  /**
   * Helper function to build an UnauthorizedError type.
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
    log.trace("AuthFilter#isAuthRequired");

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
    log.trace("AuthFilter#methodHasAuth");
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
    log.trace("AuthFilter#classHasAuth");
    return Arrays.stream(type.getDeclaredAnnotations())
      .anyMatch(Authenticated.class::isInstance);
  }

  /**
   * Annotation that flags a resource as requiring a valid user auth token to
   * execute.
   */
  @Target({ ElementType.METHOD, ElementType.TYPE })
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Authenticated {}
}
