package org.veupathdb.lib.container.jaxrs.model;

import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.ValidatedToken;
import org.json.JSONObject;

/**
 * Interface for container core users serves two purposes:
 * 1. Allows services using this library to not directly depend on oauth2 client code
 * 2. Allows future container-corespecific fields to be added
 */
public interface User extends org.gusdb.oauth2.client.veupathdb.User {

  class BasicUser extends org.gusdb.oauth2.client.veupathdb.BasicUser implements User {

    public BasicUser(long userId, boolean isGuest, String signature, String stableId) {
      super(userId, isGuest, signature, stableId);
    }

    public BasicUser(JSONObject json) {
      super(json);
    }
  }

  class BearerTokenUser extends org.gusdb.oauth2.client.veupathdb.BearerTokenUser implements User {

    public BearerTokenUser(OAuthClient client, String oauthUrl, ValidatedToken token) {
      super(client, oauthUrl, token);
    }
  }
}
