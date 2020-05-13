package org.veupathdb.lib.container.jaxrs.context;

import org.gusdb.fgputil.accountdb.UserProfile;

import javax.ws.rs.core.SecurityContext;

import java.security.Principal;

public class WdkSecurityContext implements SecurityContext {

  private final UserProfile profile;

  public WdkSecurityContext(UserProfile profile) {
    this.profile = profile;
  }

  public UserProfile getUserProfile() {
    return profile;
  }

  @Override
  public Principal getUserPrincipal() {
    return null;
  }

  @Override
  public boolean isUserInRole(String role) {
    return false;
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public String getAuthenticationScheme() {
    return null;
  }
}
