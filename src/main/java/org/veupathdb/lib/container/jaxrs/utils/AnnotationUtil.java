package org.veupathdb.lib.container.jaxrs.utils;

import jakarta.ws.rs.container.ResourceInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import static java.util.Arrays.stream;

public class AnnotationUtil {

  public static <T extends Annotation> Optional<T> findResourceAnnotation(ResourceInfo resource, Class<T> annotationClass) {
    // allow method to override class annotation, so look on method first
    return findAnnotation(resource.getResourceMethod(), annotationClass)
        .or(() -> findAnnotation(resource.getResourceClass(), annotationClass));
  }

  private static <T extends Annotation> Optional<T> findAnnotation(AnnotatedElement element, Class<T> annotationClass) {
    return stream(element.getDeclaredAnnotations())
        .filter(annotationClass::isInstance)
        .map(annotationClass::cast)
        .findFirst();
  }

}
