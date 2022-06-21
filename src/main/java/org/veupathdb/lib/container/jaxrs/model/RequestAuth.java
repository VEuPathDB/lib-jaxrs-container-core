package org.veupathdb.lib.container.jaxrs.model;

import java.util.Map;

/**
 * Request Auth Token/Header
 *
 * Wraps the authentication information for a request.
 *
 * @apiNote This type presently implements the interface {@link Map.Entry}.
 * This is temporary and was done to ease the transition of consumers of this
 * library from older versions.  When all known consumers of this library have
 * been updated to declare their types as {@link RequestAuth} instead of
 * {@code Map.Entry}, the implements should be dropped.
 *
 * @author Elizabeth Paige Harper (https://github.com/foxcapades)
 * @since v7.0.0
 */
public class RequestAuth implements Map.Entry<String, String> {
  private final String header;

  private final String token;

  public RequestAuth(String header, String token) {
    this.header = header;
    this.token  = token;
  }

  public String getHeader() {
    return header;
  }

  public String getToken() {
    return token;
  }

  @Override
  public String toString() {
    return "AuthHeader(header="+header+", secret=***)";
  }

  /**
   * Alias of {@link #getHeader()}.
   *
   * @deprecated Shift usage to {@link #getHeader()} instead of this method.
   */
  @Override
  @Deprecated
  public String getKey() {
    return header;
  }

  /**
   * Alias of {@link #getToken()}.
   *
   * @deprecated Shift usage to {@link #getToken()} instead of this method.
   */
  @Override
  @Deprecated
  public String getValue() {
    return token;
  }

  /**
   * Unsupported operation.
   */
  @Override
  @Deprecated
  public String setValue(String value) {
    throw new UnsupportedOperationException("RequestAuth instance values cannot be changed.");
  }
}
