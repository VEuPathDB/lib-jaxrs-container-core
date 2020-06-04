package org.veupathdb.lib.container.jaxrs.errors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.ClientErrorException;

/**
 * Extension for the JaxRS exceptions for 422 errors.
 */
public class UnprocessableEntityException extends ClientErrorException
{
  public static final int UNPROCESSABLE_ENTITY = 422;

  private final List < String > general;

  private final Map < String, List < String > > byKey;

  public UnprocessableEntityException(String message) {
    super(UNPROCESSABLE_ENTITY);
    general = new ArrayList <>();
    byKey = new HashMap <>();

    general.add(message);
  }

  public UnprocessableEntityException(Throwable cause) {
    super(UNPROCESSABLE_ENTITY, cause);
    general = new ArrayList <>();
    byKey = new HashMap <>();

    general.add(cause.getMessage());
  }

  public UnprocessableEntityException(String message, Throwable cause) {
    super(UNPROCESSABLE_ENTITY, cause);
    general = new ArrayList <>();
    byKey = new HashMap <>();

    general.add(message);
  }

  public UnprocessableEntityException(Map < String, List < String > > byKey) {
    super(UNPROCESSABLE_ENTITY);
    this.general = new ArrayList <>();
    this.byKey = byKey;
  }

  public UnprocessableEntityException(
    final List < String > general,
    final Map < String, List < String > > byKey
  ) {
    super(UNPROCESSABLE_ENTITY);
    this.general = general;
    this.byKey = byKey;
  }

  public List < String > getGeneral() {
    return general;
  }

  public Map < String, List < String > > getByKey() {
    return byKey;
  }
}
