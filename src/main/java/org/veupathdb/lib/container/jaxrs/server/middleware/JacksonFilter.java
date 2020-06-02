package org.veupathdb.lib.container.jaxrs.server.middleware;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.annotation.Priority;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.veupathdb.lib.container.jaxrs.errors.UnprocessableEntityException;

@Provider
@Priority(5)
public class JacksonFilter
implements MessageBodyReader < Object >, MessageBodyWriter < Object >
{
  private static final String SUBTYPE = "json";

  private static final ObjectMapper JSON = new ObjectMapper();

  @Override
  public boolean isReadable(
    final Class < ? > type,
    final Type genericType,
    final Annotation[] annotations,
    final MediaType mediaType
  ) {
    return SUBTYPE.equals(mediaType.getSubtype());
  }

  @Override
  public boolean isWriteable(
    final Class < ? > type,
    final Type genericType,
    final Annotation[] annotations,
    final MediaType mediaType
  ) {
    return SUBTYPE.equals(mediaType.getSubtype());
  }

  @Override
  public void writeTo(
    final Object o,
    final Class < ? > type,
    final Type genericType,
    final Annotation[] annotations,
    final MediaType mediaType,
    final MultivaluedMap < String, Object > httpHeaders,
    final OutputStream entityStream
  ) throws IOException, WebApplicationException {
    JSON.writeValue(entityStream, o);
  }

  @Override
  public Object readFrom(
    final Class < Object > type,
    final Type genericType,
    final Annotation[] annotations,
    final MediaType mediaType,
    final MultivaluedMap < String, String > httpHeaders,
    final InputStream entityStream
  ) throws IOException, WebApplicationException {
    try {
      return JSON.readValue(entityStream, type);
    } catch (JsonParseException e) {
      throw new BadRequestException(e.getMessage());
    } catch (JsonMappingException e) {
      throw new UnprocessableEntityException(new HashMap <String, List <String> >(){{
        put(e.getPath().get(0).getFieldName(), new ArrayList <>()
        {{
          add(e.getMessage()
            .split(" at")[0]
            .replaceAll(
              " \\(class [^)]+\\)",
              ""
            ));
        }});
      }});
    }
  }


}
