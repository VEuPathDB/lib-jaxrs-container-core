package org.veupathdb.lib.container.jaxrs.server.middleware;

import javax.annotation.Priority;
import javax.ws.rs.*;
import org.glassfish.jersey.server.ParamException.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

import org.veupathdb.lib.container.jaxrs.errors.UnprocessableEntityException;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;
import org.veupathdb.lib.container.jaxrs.utils.RequestKeys;
import org.veupathdb.lib.container.jaxrs.view.error.*;

import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

@Provider
@PreMatching
@Priority(1)
public class ErrorMapper implements ExceptionMapper<Throwable> {

  private interface Mapper {
    ErrorResponse toError(Throwable t);
  }

  private final Map<Class<? extends Throwable>, Mapper> mappers = new HashMap<>() {{
    put(BadRequestException.class,  BadRequestError::new);
    put(QueryParamException.class,  BadRequestError::new);
    put(HeaderParamException.class, BadRequestError::new);
    put(CookieParamException.class, BadRequestError::new);
    put(FormParamException.class,   BadRequestError::new);

    put(NotAuthorizedException.class, UnauthorizedError::new);
    put(ForbiddenException.class,     ForbiddenError::new);

    put(NotFoundException.class,  NotFoundError::new);
    put(PathParamException.class, NotFoundError::new);

    put(NotAllowedException.class,          BadMethodError::new);
    put(NotSupportedException.class,        BadContentTypeError::new);
    put(UnprocessableEntityException.class, InvalidInputError::new);
    put(InternalServerErrorException.class, ErrorMapper.this::serverError);
  }};

  @Context
  ContainerRequestContext ctx;

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

    var mapper = mappers.get(err.getClass());

    return Response.status(code)
      .entity((mapper != null
        ? mapper.toError(err)
        : (err instanceof WebApplicationException ? err : this.serverError(err))))
      .type(MediaType.APPLICATION_JSON_TYPE)
      .build();
  }

  private ErrorResponse serverError(Throwable error) {
    return new ServerError(
      (String) ctx.getProperty(RequestKeys.REQUEST_ID),
      error
    );
  }
}
