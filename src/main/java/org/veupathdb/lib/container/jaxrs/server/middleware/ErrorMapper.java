package org.veupathdb.lib.container.jaxrs.server.middleware;

import java.util.HashMap;
import java.util.Map;

import io.prometheus.client.Counter;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.Request;
import org.veupathdb.lib.container.jaxrs.Globals;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.utils.logging.LoggingVars;
import org.veupathdb.lib.container.jaxrs.view.error.*;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
@PreMatching
@Priority(1)
public class ErrorMapper implements ExceptionMapper<Throwable> {
  public static final Counter INTERNAL_ERROR_COUNT = Counter.build()
      .name("internal_error")
      .help("Count of internal errors when executing service endpoints.")
      .register();

  private Logger log = LogProvider.logger(getClass());

  private interface Mapper {
    ErrorResponse toError(Throwable t);
  }

  private final Map<Integer, Mapper> mappers = new HashMap<>() {{
    put(400, BadRequestError::new);
    put(401, UnauthorizedError::new);
    put(403, ForbiddenError::new);
    put(404, NotFoundError::new);
    put(405, BadMethodError::new);
    put(409, ConflictError::new);
    put(410, GoneError::new);
    put(415, BadContentTypeError::new);
    put(422, InvalidInputError::new);
    put(500, ErrorMapper.this::serverError);
  }};

  @Inject
  private jakarta.inject.Provider<Request> _request;

  public ErrorMapper() {
    // Emit a 0 to initialize the metric so the first increase can be detected.
    INTERNAL_ERROR_COUNT.inc(0);
  }

  @Override
  public Response toResponse(Throwable err) {
    log.trace("toResponse(err={})", () -> err);

    var code = err instanceof WebApplicationException
      ? ((WebApplicationException) err).getResponse().getStatus()
      : INTERNAL_SERVER_ERROR.getStatusCode();

    if (code == INTERNAL_SERVER_ERROR.getStatusCode()) {
      // Log the trace-id, since it's being removed from the ThreadContext before this point.
      // It's unclear why it's being removed from the context before this is invoked, but this ensures we have it.
      String traceId = _request.get().getHeader(Globals.TRACE_ID_HEADER);
      log.error("(trace: {}) Caught Exception: ", traceId, err);

      // If final response is 5XX, emit a metric.
      INTERNAL_ERROR_COUNT.inc();
    } else {
      log.debug("Caught Exception: ", err);
    }

    var mapper = mappers.get(code);

    return Response.status(code)
      .entity((mapper != null
        ? mapper.toError(err)
        : (err instanceof WebApplicationException ? err : this.serverError(err))))
      .type(MediaType.APPLICATION_JSON_TYPE)
      .build();
  }

  private ErrorResponse serverError(Throwable error) {
    log.trace("serverError(error={})", () -> error);

    return new ServerError(
      (String) _request.get().getAttribute(RequestKeys.REQUEST_ID),
      error
    );
  }
}
