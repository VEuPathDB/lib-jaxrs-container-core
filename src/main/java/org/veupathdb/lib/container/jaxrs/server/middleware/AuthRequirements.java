package org.veupathdb.lib.container.jaxrs.server.middleware;

import jakarta.ws.rs.container.ResourceInfo;
import org.veupathdb.lib.container.jaxrs.server.annotations.AdminRequired;
import org.veupathdb.lib.container.jaxrs.server.annotations.Authenticated;
import org.veupathdb.lib.container.jaxrs.utils.AnnotationUtil;

import java.util.Optional;

class AuthRequirements {

  // flags indicating whether to probe submitted values
  public final boolean adminDiscoveryRequired;
  public final boolean userDiscoveryRequired;

  // flags indicating how to handle submitted values
  public final boolean                           adminRequired;
  public final boolean                           guestsAllowed;
  public final Authenticated.AdminOverrideOption overrideOption;

  AuthRequirements(ResourceInfo resource) {
    Optional<Authenticated> authAnnotationOpt  = AnnotationUtil.findResourceAnnotation(resource, Authenticated.class);
    Optional<AdminRequired> adminAnnotationOpt = AnnotationUtil.findResourceAnnotation(resource, AdminRequired.class);
    adminRequired = adminAnnotationOpt.isPresent();
    userDiscoveryRequired = authAnnotationOpt.isPresent();
    overrideOption = authAnnotationOpt.map(Authenticated::adminOverride).orElse(Authenticated.AdminOverrideOption.DISALLOW);
    adminDiscoveryRequired = adminRequired || overrideOption != Authenticated.AdminOverrideOption.DISALLOW;
    guestsAllowed = authAnnotationOpt.isPresent() && authAnnotationOpt.get().allowGuests();
  }
}
