package org.veupathdb.lib.container.jaxrs.model;

import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.ValidatedToken;

/**
 * Interface for container core users serves two purposes:
 * 1. Allows services using this library to not directly depend on oauth2 client code
 * 2. Allows future container-core-specific fields to be added
 */
public interface User extends org.gusdb.oauth2.client.veupathdb.User, UserInfo {

  // provides ability to access raw token value without directly depending on OAuth library
  default String getAuthenticationTokenValue() {
    return getAuthenticationToken().getTokenValue();
  }

  class UserImpl extends org.gusdb.oauth2.client.veupathdb.UserImpl implements User {

    public UserImpl(OAuthClient client, String oauthUrl, ValidatedToken token) {
      super(client, oauthUrl, token);
    }
  }
}
