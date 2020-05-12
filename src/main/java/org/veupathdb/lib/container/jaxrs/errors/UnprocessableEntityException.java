package org.veupathdb.lib.container.jaxrs.errors;

import javax.ws.rs.ClientErrorException;

/**
 * Extension for the JaxRS exceptions for 422 errors.
 */
public class UnprocessableEntityException extends ClientErrorException {
  public static final int UNPROCESSABLE_ENTITY = 422;

  public UnprocessableEntityException(String message) {
    super(message, UNPROCESSABLE_ENTITY);
  }

  public UnprocessableEntityException(Throwable cause) {
    super(UNPROCESSABLE_ENTITY, cause);
  }

  public UnprocessableEntityException(String message, Throwable cause) {
    super(message, UNPROCESSABLE_ENTITY, cause);
  }
}
