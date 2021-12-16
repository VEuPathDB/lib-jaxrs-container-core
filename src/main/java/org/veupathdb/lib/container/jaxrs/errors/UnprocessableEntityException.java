package org.veupathdb.lib.container.jaxrs.errors;

import jakarta.ws.rs.ClientErrorException;

import java.util.*;

/**
 * Extension for the JaxRS exceptions for 422 errors.
 */
public class UnprocessableEntityException extends ClientErrorException {
  public static final int UNPROCESSABLE_ENTITY = 422;

  private final List<String> general;

  private final Map<String, List<String>> byKey;

  public UnprocessableEntityException(String message) {
    super(UNPROCESSABLE_ENTITY);
    general = new ArrayList<>();
    byKey   = new HashMap<>();

    general.add(message);
  }

  public UnprocessableEntityException(Throwable cause) {
    super(UNPROCESSABLE_ENTITY, cause);
    general = new ArrayList<>();
    byKey   = new HashMap<>();

    general.add(cause.getMessage());
  }

  public UnprocessableEntityException(String message, Throwable cause) {
    super(UNPROCESSABLE_ENTITY, cause);
    general = new ArrayList<>();
    byKey   = new HashMap<>();

    general.add(message);
  }

  public UnprocessableEntityException(Map<String, List<String>> byKey) {
    super(UNPROCESSABLE_ENTITY);
    this.general = new ArrayList<>();
    this.byKey   = byKey;
  }

  public UnprocessableEntityException(
    final List<String> general,
    final Map<String, List<String>> byKey
  ) {
    super(UNPROCESSABLE_ENTITY);
    this.general = general;
    this.byKey   = byKey;
  }

  public List<String> getGeneral() {
    return general;
  }

  public Map<String, List<String>> getByKey() {
    return byKey;
  }

  /**
   * Keyed Single returns a new {@code UnprocessableEntityException} with a
   * single error key mapped to a single error message.
   *
   * @param key   Error Key.
   * @param error Error Message
   *
   * @return new {@code UnprocessableEntityException}.
   */
  public static UnprocessableEntityException keyedSingle(String key, String error) {
    return new UnprocessableEntityException(
      Collections.singletonMap(key, Collections.singletonList(error))
    );
  }

  /**
   * Keyed singles returns a new {@code UnprocessableEntityException} with two
   * error keys, each mapped to a single error message.
   *
   * @param key1   First error key.
   * @param error1 First error message.
   * @param key2   Second error key.
   * @param error2 Second error message.
   *
   * @return new {@code UnprocessableEntityException}.
   */
  public static UnprocessableEntityException keyedSingles(
    String key1, String error1,
    String key2, String error2
  ) {
    return new UnprocessableEntityException(new HashMap<>() {{
      put(key1, Collections.singletonList(error1));
      put(key2, Collections.singletonList(error2));
    }});
  }

  /**
   * Keyed singles returns a new {@code UnprocessableEntityException} with three
   * error keys, each mapped to a single error message.
   *
   * @param key1   First error key.
   * @param error1 First error message.
   * @param key2   Second error key.
   * @param error2 Second error message.
   * @param key3   Third error key.
   * @param error3 Third error message.
   *
   * @return new {@code UnprocessableEntityException}.
   */
  public static UnprocessableEntityException keyedSingles(
    String key1, String error1,
    String key2, String error2,
    String key3, String error3
  ) {
    return new UnprocessableEntityException(new HashMap<>() {{
      put(key1, Collections.singletonList(error1));
      put(key2, Collections.singletonList(error2));
      put(key3, Collections.singletonList(error3));
    }});
  }
}
