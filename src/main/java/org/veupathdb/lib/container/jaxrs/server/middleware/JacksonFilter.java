package org.veupathdb.lib.container.jaxrs.server.middleware;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Priority;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.veupathdb.lib.container.jaxrs.errors.UnprocessableEntityException;

/**
 * Jackson JSON (De)Serialization Filter
 * <p>
 * Serializes non-text response bodies to JSON.  Deserializes JSON request
 * bodies into the handler method's declared input type.
 * <p>
 * This filter will only kick in if the following conditions are met:
 * <ol>
 *   <li>For requests and responses: the <code>Content-Type</code> header
 *   matches the pattern "<code>^.+/json$</code>"</li>
 *   <li>
 *     For responses: the handler method return type is not one of:
 *     <ul>
 *       <li><code>String</code></li>
 *       <li><code>CharSequence</code></li>
 *     </ul>
 *   </li>
 * </ol>
 */
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
      if (List.class.isAssignableFrom(type)) {
        var pType = (ParameterizedType) genericType;
        return inputToStream(entityStream, (Class < ? >) pType.getActualTypeArguments()[0])
          .collect(Collectors.toUnmodifiableList());
      }

      if (Set.class.isAssignableFrom(type)) {
        var pType = (ParameterizedType) genericType;
        return inputToStream(entityStream, (Class < ? >) pType.getActualTypeArguments()[0])
          .collect(Collectors.toUnmodifiableSet());
      }

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

  public < T > Stream < T > inputToStream(final InputStream input, final Class < T > cls)
  throws IOException {
    return StreamSupport.stream(
      Spliterators.spliteratorUnknownSize(
        JSON.readValues(JSON.createParser(input), cls),
        Spliterator.ORDERED
      ),
      false
    );
  }
}
