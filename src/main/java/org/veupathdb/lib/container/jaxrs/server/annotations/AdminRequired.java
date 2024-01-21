package org.veupathdb.lib.container.jaxrs.server.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that flags a resource as requiring an admin token value before
 * processing.  When used, a resource method will not run, and a 403 Forbidden
 * response will be returned.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AdminRequired {

}
