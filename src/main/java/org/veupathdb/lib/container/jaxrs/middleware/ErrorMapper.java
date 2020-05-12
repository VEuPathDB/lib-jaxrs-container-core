package org.veupathdb.lib.container.jaxrs.middleware;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

import java.util.HashMap;
import java.util.Map;

import org.veupathdb.lib.container.jaxrs.errors.UnprocessableEntityException;
import org.veupathdb.lib.container.jaxrs.view.error.*;

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
      : Status.INTERNAL_SERVER_ERROR.getStatusCode();

    return Response.status(code)
      .entity(mappers.getOrDefault(err.getClass(), ServerError::new).toError(err))
      .type(MediaType.APPLICATION_JSON_TYPE)
      .build();
  }
}
