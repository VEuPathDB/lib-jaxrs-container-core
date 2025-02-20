package org.veupathdb.lib.container.jaxrs.model;

import org.json.JSONObject;

/**
 * Interface for container core users serves two purposes:
 * 1. Allows services using this library to not directly depend on oauth2 client code
 * 2. Allows future container-core-specific fields to be added
 */
public interface UserInfo extends org.gusdb.oauth2.client.veupathdb.UserInfo {

  class UserInfoImpl extends org.gusdb.oauth2.client.veupathdb.UserInfoImpl implements UserInfo {

    public UserInfoImpl(JSONObject json) {
      super(json);
    }
  }
}
