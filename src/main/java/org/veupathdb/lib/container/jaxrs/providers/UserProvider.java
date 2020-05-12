package org.veupathdb.lib.container.jaxrs.providers;

import org.gusdb.fgputil.accountdb.UserProfile;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.veupathdb.lib.container.jaxrs.Globals;

/**
 * Context Resolver for UserProfiles in authenticated requests.
 */
@Provider
public class UserProvider implements ContextResolver<UserProfile> {

  @Context
  ContainerRequestContext context;

  @Override
  public UserProfile getContext(Class<?> type) {
    return (UserProfile) context.getProperty(Globals.REQUEST_USER);
  }
}
