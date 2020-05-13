package org.veupathdb.lib.container.jaxrs.providers;

import org.gusdb.fgputil.accountdb.UserProfile;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.ext.ContextResolver;

import java.util.Optional;

import org.veupathdb.lib.container.jaxrs.Globals;

/**
 * Context Resolver for UserProfiles in authenticated requests.
 */
public class UserProvider implements ContextResolver<UserProvider> {

//  private static final byte TIMEOUT_HOURS = 2;
//  private static final byte INTERVAL_MINS = 10;

  private static UserProvider instance;

  @Inject
  private Provider<ContainerRequestContext> ctx;

  private UserProvider() {}

  public Optional<UserProfile> getUser() {
    return Optional.ofNullable((UserProfile) ctx.get()
      .getProperty(Globals.REQUEST_USER));
  }

  @Override
  public UserProvider getContext(Class<?> type) {
    return instance;
  }

  public static UserProvider getInstance() {
    if (instance == null)
      instance = new UserProvider();
    return instance;
  }
}
