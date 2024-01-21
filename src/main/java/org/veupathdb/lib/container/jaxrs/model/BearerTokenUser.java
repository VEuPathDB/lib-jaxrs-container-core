package org.veupathdb.lib.container.jaxrs.model;

import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.ValidatedToken;
import org.gusdb.oauth2.shared.token.IdTokenFields;
import org.json.JSONObject;

public class BearerTokenUser extends User {

  private final OAuthClient _client;
  private final ValidatedToken _token;
  private final String _oauthBaseUrl;
  private boolean _userInfoFetched = false;

  public BearerTokenUser(OAuthClient client, String oauthBaseUrl, ValidatedToken token) {
    _client = client;
    _oauthBaseUrl = oauthBaseUrl;
    _token = token;
    // set immutable fields provided on the token
    setUserID(Long.valueOf(_token.getUserId()));
    setGuest(_token.isGuest());
    setSignature(_token.getTokenContents().get(IdTokenFields.signature.name(), String.class));
    setStableID(_token.getTokenContents().get(IdTokenFields.preferred_username.name(), String.class));
  }

  protected void fetchUserInfo() {
    // return if already fetched
    if (_userInfoFetched) return;

    // fetch user info from OAuth server where it is stored (but only on demand, and only once for this object's lifetime)
    JSONObject userInfo = _client.getUserData(_oauthBaseUrl, _token);

    setEmail(userInfo.getString(IdTokenFields.email.name()));
    setFirstName(userInfo.optString("firstName", null));
    setMiddleName(userInfo.optString("middleName", null));
    setLastName(userInfo.optString("lastName", null));
    setOrganization(userInfo.optString("organization", null));

    _userInfoFetched = true;
  }
}
