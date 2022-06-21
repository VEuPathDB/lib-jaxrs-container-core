package org.veupathdb.lib.container.jaxrs.model;

import java.util.Map;

/**
 * Request Auth Token/Header
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

  @Override
  @Deprecated
  public String getKey() {
    return header;
  }

  @Override
  @Deprecated
  public String getValue() {
    return token;
  }

  @Override
  @Deprecated
  public String setValue(String value) {
    throw new UnsupportedOperationException("RequestAuth instance values cannot be changed.");
  }
}
