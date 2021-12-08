package org.veupathdb.lib.container.jaxrs.server.middleware;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import org.veupathdb.lib.container.jaxrs.view.error.ErrorResponse;

@Provider
public class NonJsonExceptionWriter implements MessageBodyWriter {

  @Override
  public boolean isWriteable(Class type, Type genericType, Annotation[] annotations, MediaType mediaType) {
    return ErrorResponse.class.isAssignableFrom(type) && !MediaType.APPLICATION_JSON.equals(mediaType);
  }

  @Override
  public void writeTo(Object o, Class type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
    entityStream.write(((ErrorResponse)o).getMessage().getBytes(StandardCharsets.UTF_8));
  }
}
