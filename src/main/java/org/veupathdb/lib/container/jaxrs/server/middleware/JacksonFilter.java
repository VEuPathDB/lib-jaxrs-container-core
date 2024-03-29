package org.veupathdb.lib.container.jaxrs.server.middleware;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import jakarta.annotation.Priority;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import org.veupathdb.lib.container.jaxrs.errors.UnprocessableEntityException;
import org.veupathdb.lib.container.jaxrs.server.annotations.DisableJackson;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.veupathdb.lib.container.jaxrs.view.error.ErrorResponse;

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
  implements MessageBodyReader<Object>, MessageBodyWriter<Object>
{
  private static final String SUBTYPE = "json";

  private static final ObjectMapper JSON;

  static {
    JSON = JsonMapper.builder()
      .addModule(new ParameterNamesModule())
      .addModule(new Jdk8Module())
      .addModule(new JavaTimeModule())
      .build();

    JSON.setDateFormat(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
    JSON.getDateFormat().setTimeZone(TimeZone.getDefault());
  }

  @Context
  private ResourceInfo res;

  @Override
  public boolean isReadable(
    final Class<?> type,
    final Type genericType,
    final Annotation[] annotations,
    final MediaType mediaType
  ) {
    return SUBTYPE.equals(mediaType.getSubtype());
  }

  @Override
  public boolean isWriteable(
    final Class<?> type,
    final Type genericType,
    final Annotation[] annotations,
    final MediaType mediaType
  ) {
    // If it's an error type, kick in always
    if (ErrorResponse.class.isAssignableFrom(type))
      return true;
    if (Exception.class.isAssignableFrom(type))
      return true;

    // bail if it's not a json target
    if (!SUBTYPE.equals(mediaType.getSubtype()))
      return false;

    return Arrays.stream(annotations).noneMatch(a -> a instanceof DisableJackson);
  }

  @Override
  public void writeTo(
    final Object o,
    final Class<?> type,
    final Type genericType,
    final Annotation[] annotations,
    final MediaType mediaType,
    final MultivaluedMap<String, Object> httpHeaders,
    final OutputStream entityStream
  ) throws IOException, WebApplicationException {
    JSON.writeValue(entityStream, o);
  }

  @Override
  public Object readFrom(
    final Class<Object> type,
    final Type genericType,
    final Annotation[] annotations,
    final MediaType mediaType,
    final MultivaluedMap<String, String> httpHeaders,
    final InputStream entityStream
  ) throws IOException, WebApplicationException {
    try {
      if (List.class.isAssignableFrom(type)) {
        var pType = (ParameterizedType) genericType;
        @SuppressWarnings("unchecked")
        var typeFac = JSON.getTypeFactory()
          .constructCollectionType(
            (Class<? extends List<?>>) ((Class<?>) type),
            (Class<?>) pType.getActualTypeArguments()[0]
          );
        return JSON.readValue(entityStream, typeFac);
      }

      return JSON.readValue(entityStream, type);
    } catch (JsonParseException e) {
      throw new BadRequestException(e.getMessage());
    } catch (JsonMappingException e) {
      var message = e.getMessage()
        .split(" at")[0]
        .replaceAll(" \\(class [^)]+\\)", "");

      if (e.getPath() != null && e.getPath().isEmpty())
        throw new UnprocessableEntityException(
          Collections.singletonList(message),
          Collections.emptyMap()
        );

      throw new UnprocessableEntityException(Collections.singletonMap(
        e.getPath().get(0).getFieldName(), Collections.singletonList(message)));
    }
  }
}
