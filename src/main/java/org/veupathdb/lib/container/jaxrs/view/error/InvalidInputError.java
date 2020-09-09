package org.veupathdb.lib.container.jaxrs.view.error;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonGetter;
import org.veupathdb.lib.container.jaxrs.errors.UnprocessableEntityException;

public class InvalidInputError extends ErrorResponse
{
  public static final String
    JSON_KEY_ERRORS = "errors";

  private final Errors errors;

  public InvalidInputError() {
    super(ErrorStatus.UNPROCESSABLE_ENTITY);
    errors = new Errors(Collections.emptyList(), Collections.emptyMap());
  }

  public InvalidInputError(String message) {
    super(ErrorStatus.UNPROCESSABLE_ENTITY);
    errors = new Errors(Collections.singletonList(message), Collections.emptyMap());
  }

  public InvalidInputError(Throwable error) {
    super(ErrorStatus.UNPROCESSABLE_ENTITY);
    if (error instanceof UnprocessableEntityException) {
      var e = (UnprocessableEntityException) error;
      errors = new Errors(new ArrayList <>(e.getGeneral()), new HashMap <>(e.getByKey()));
    } else {
      errors = new Errors(Collections.singletonList(error.getMessage()), Collections.emptyMap());
    }
  }

  @JsonGetter(JSON_KEY_ERRORS)
  public Errors getErrors() {
    return errors;
  }

  public static class Errors
  {
    public static final String
      JSON_KEY_GENERAL = "general",
      JSON_KEY_BY_KEY  = "byKey";

    private final List < String > general;

    private final Map < String, List < String > > byKey;

    public Errors(
      List < String > general,
      Map < String, List < String > > byKey
    ) {
      this.general = Collections.unmodifiableList(general);
      this.byKey   = Collections.unmodifiableMap(byKey);
    }

    @JsonGetter(JSON_KEY_GENERAL)
    public List < String > getGeneral() {
      return general;
    }

    @JsonGetter(JSON_KEY_BY_KEY)
    public Map < String, List < String > > getByKey() {
      return byKey;
    }
  }
}
