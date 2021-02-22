package org.veupathdb.lib.container.jaxrs.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that flags a resource as requiring a valid user auth token to
 * execute.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated
{
  boolean allowGuests() default false;
}
