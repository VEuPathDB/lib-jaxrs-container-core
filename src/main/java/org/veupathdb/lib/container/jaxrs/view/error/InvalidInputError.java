package org.veupathdb.lib.container.jaxrs.view.error;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.veupathdb.lib.container.jaxrs.errors.UnprocessableEntityException;

public class InvalidInputError extends ErrorResponse
{

  private final List < String > general;

  private final Map < String, List < String > > byKey;

  public InvalidInputError() {
    super(ErrorStatus.UNPROCESSABLE_ENTITY);
    general = new ArrayList <>();
    byKey   = new HashMap <>();
  }

  public InvalidInputError(String message) {
    this();
    general.add(message);
  }

  public InvalidInputError(Throwable error) {
    this();
    if (error instanceof UnprocessableEntityException) {
      var e = (UnprocessableEntityException) error;
      this.byKey.putAll(e.getByKey());
      this.general.addAll(e.getGeneral());
    } else {
      this.general.add(error.getMessage());
    }
  }

  public List < String > getGeneral() {
    return general;
  }

  public Map < String, List < String > > getByKey() {
    return byKey;
  }
}
