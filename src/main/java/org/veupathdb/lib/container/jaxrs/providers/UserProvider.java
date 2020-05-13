package org.veupathdb.lib.container.jaxrs.providers;

import org.gusdb.fgputil.accountdb.UserProfile;

import javax.inject.Provider;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;

import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;

/**
 * Context Resolver for UserProfiles in authenticated requests.
 */
public class UserProvider implements ContextResolver<UserProfile> {

  @Context
  private Provider<ContainerRequestContext> req;

  @Override
  public UserProfile getContext(Class<?> type) {
    return (UserProfile) req.get().getProperty(RequestKeys.REQUEST_ID);
  }
}
