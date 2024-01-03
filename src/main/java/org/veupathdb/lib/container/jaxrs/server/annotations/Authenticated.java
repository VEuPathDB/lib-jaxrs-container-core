package org.veupathdb.lib.container.jaxrs.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that flags a resource as requiring valid user authentication to
 * execute.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated
{

  enum AdminOverrideOption {

    /** (default if missing) admin is irrelevant */
    DISALLOW,

    /** allow access only if admin token is present and valid and proxied-user-id header/query-param is present */
    ALLOW_WITH_USER,

    /** allow access only if admin token is present; proxied user discovery will be attempted via
     * proxied-user-id header/query-param but resource will be visited with an empty user if missing */
    ALLOW_ALWAYS
  }

  /**
   * Whether to allow guest users to access this resource.  Defaults to false.
   *
   * @return whether guest users are allowed to access this resource
   */
  boolean allowGuests() default false;

  /**
   * Applies an override option to this resource.  Defaults to DISALLOW.
   *
   * @return which {@link AdminOverrideOption} applies to this resource
   */
  AdminOverrideOption adminOverride() default AdminOverrideOption.DISALLOW;
}
