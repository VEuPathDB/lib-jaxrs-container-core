package org.veupathdb.lib.container.jaxrs.providers;

import org.gusdb.fgputil.accountdb.UserProfile;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import java.util.Optional;

import org.veupathdb.lib.container.jaxrs.context.WdkSecurityContext;

/**
 * Context Resolver for UserProfiles in authenticated requests.
 */
@Provider
public class UserProvider implements ContextResolver<Optional<UserProfile>> {

//  private static final byte TIMEOUT_HOURS = 2;
//  private static final byte INTERVAL_MINS = 10;

  private static UserProvider instance;

//  private Map<String, >

  @Context
  private SecurityContext ctx;

  private UserProvider() {}

  @Override
  public Optional<UserProfile> getContext(Class<?> type) {
    return Optional.ofNullable(ctx)
      .filter(WdkSecurityContext.class::isInstance)
      .map(WdkSecurityContext.class::cast)
      .map(WdkSecurityContext::getUserProfile);
  }

  public static UserProvider getInstance() {
    if (instance == null)
      instance = new UserProvider();
    return instance;
  }
}
