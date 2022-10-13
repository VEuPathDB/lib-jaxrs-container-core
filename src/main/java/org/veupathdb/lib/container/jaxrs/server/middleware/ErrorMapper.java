package org.veupathdb.lib.container.jaxrs.server.middleware;

import java.util.HashMap;
import java.util.Map;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.NotSupportedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.jersey.server.ParamException.CookieParamException;
import org.glassfish.jersey.server.ParamException.FormParamException;
import org.glassfish.jersey.server.ParamException.HeaderParamException;
import org.glassfish.jersey.server.ParamException.PathParamException;
import org.glassfish.jersey.server.ParamException.QueryParamException;
import org.veupathdb.lib.container.jaxrs.errors.UnprocessableEntityException;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.view.error.*;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
@PreMatching
@Priority(1)
public class ErrorMapper implements ExceptionMapper<Throwable> {

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
    put(415, BadContentTypeError::new);
    put(422, InvalidInputError::new);
    put(500, ErrorMapper.this::serverError);
  }};

  @Inject
  private jakarta.inject.Provider<Request> _request;

  @Override
  public Response toResponse(Throwable err) {
    var code = err instanceof WebApplicationException
      ? ((WebApplicationException) err).getResponse().getStatus()
      : INTERNAL_SERVER_ERROR.getStatusCode();

    if (code == INTERNAL_SERVER_ERROR.getStatusCode()) {
      LogProvider.logger(ErrorMapper.class).warn("Caught Exception: ", err);
    } else {
      LogProvider.logger(ErrorMapper.class).debug("Caught Exception: {}", err.getMessage());
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
    return new ServerError(
      (String) _request.get().getAttribute(RequestKeys.REQUEST_ID),
      error
    );
  }
}
