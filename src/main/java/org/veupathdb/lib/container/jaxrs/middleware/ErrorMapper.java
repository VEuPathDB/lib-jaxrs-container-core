package org.veupathdb.lib.container.jaxrs.middleware;

import javax.ws.rs.*;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import java.util.HashMap;
import java.util.Map;

import org.veupathdb.lib.container.jaxrs.errors.UnprocessableEntityException;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.view.error.*;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@PreMatching
public class ErrorMapper implements ExceptionMapper<Throwable> {
  private interface Mapper {
    ErrorResponse toError(Throwable t);
  }

  private final Map<Class<? extends Throwable>, Mapper> mappers = new HashMap<>() {{
    put(BadRequestException.class, BadRequestError::new);
    put(NotAuthorizedException.class, UnauthorizedError::new);
    put(ForbiddenException.class, ForbiddenError::new);
    put(NotFoundException.class, NotFoundError::new);
    put(NotAllowedException.class, BadMethodError::new);
    put(NotSupportedException.class, BadContentTypeError::new);
    put(UnprocessableEntityException.class, InvalidInputError::new);
    put(InternalServerErrorException.class, ServerError::new);
  }};

  @Override
  public Response toResponse(Throwable err) {
    var code = err instanceof WebApplicationException
      ? ((WebApplicationException) err).getResponse().getStatus()
      : INTERNAL_SERVER_ERROR.getStatusCode();

    if (code == INTERNAL_SERVER_ERROR.getStatusCode()) {
      LogProvider.logger(ErrorMapper.class).warn("Caught Exception: ", err);
    } else {
      LogProvider.logger(ErrorMapper.class).debug("Caught Exception: ", err);
    }

    return Response.status(code)
      .entity(mappers.getOrDefault(err.getClass(), ServerError::new).toError(err))
      .type(MediaType.APPLICATION_JSON_TYPE)
      .build();
  }
}
