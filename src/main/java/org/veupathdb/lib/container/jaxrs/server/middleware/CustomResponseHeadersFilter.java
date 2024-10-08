package org.veupathdb.lib.container.jaxrs.server.middleware;

import java.util.Map;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

/**
 * Provides a mechanism for services classes that implement generated service
 * interfaces (via RAML) to set response headers.  To do so, the service should
 * get an injected ContainerRequestContext and add a property with name equal
 * to this class's CUSTOM_HEADERS_KEY constant.  The value should be a
 * Map&lt;String,String&gt; containing desired header name/value entries.
 */
@Provider
public class CustomResponseHeadersFilter implements ContainerResponseFilter {

  private static Logger LOG = LogProvider.logger(CustomResponseHeadersFilter.class);

  /**
   * Attribute service classes should fill on the ContainerRequestContext
   * to set custom headers on the response.
   */
  public static final String CUSTOM_HEADERS_KEY = "CUSTOM_HEADERS";

  @Override
  public void filter(ContainerRequestContext req, ContainerResponseContext res) {
    Object o = req.getProperty(CUSTOM_HEADERS_KEY);
    if (o == null) return;
    if (o instanceof Map) {
      for (Map.Entry<?,?> entry : ((Map<?,?>)o).entrySet()) {
        if (!(entry.getKey() instanceof String)) {
          throw makeTypeException("entry key", entry.getKey(), String.class);
        }
        if (!(entry.getValue() instanceof String)) {
          throw makeTypeException("entry value", entry.getValue(), String.class);
        }
        res.getHeaders().add((String)entry.getKey(), (String)entry.getValue());
      }
    }
    else {
      throw makeTypeException("map", o, Map.class);
    }
  }

  private RuntimeException makeTypeException(String name, Object obj, Class<?> requiredClass) {
    RuntimeException e = new RuntimeException("Invalid type sent for '" + CUSTOM_HEADERS_KEY + "' " + name +
        "; must be " + requiredClass.getName() + ", but was " + (obj == null ? "null" : obj.getClass().getName()));
    //noinspection StringConcatenationArgumentToLogCall
    LOG.error("Bad property sent to " + CustomResponseHeadersFilter.class.getSimpleName(), e);
    return e;
  }

}
