package org.veupathdb.lib.container.jaxrs.providers;

import org.gusdb.oauth2.client.KeyStoreTrustManager;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.OAuthConfig;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;

import javax.net.ssl.TrustManager;

public class OAuthProvider {

  public static final String DEFAULT_OAUTH_URL = "https://integrate.eupathdb.org/oauth";

  public static String getOAuthUrl() {
    return OptionsProvider.getOptions().getOAuthUrl().orElse(OAuthProvider.DEFAULT_OAUTH_URL);
  }

  public static OAuthClient getOAuthClient() {
    // TODO: may need to read key store environment vars via OptionsProvider.getOptions() to read mounted key store file
    TrustManager tm = new KeyStoreTrustManager();

    return new OAuthClient(tm);
  }

  public static OAuthConfig getOAuthConfig() {
    String oauthUrl = getOAuthUrl();

    Options options = OptionsProvider.getOptions();

    String clientId = options.getOAuthClientId().orElseThrow(() ->
        new InvalidConfigException("Client ID is required for this service"));
    String clientSecret = options.getOAuthClientSecret().orElseThrow(() ->
        new InvalidConfigException("Client secret is required for this service"));

    return new OAuthConfig() {
      @Override public String getOauthUrl() { return oauthUrl; }
      @Override public String getOauthClientId() { return clientId; }
      @Override public String getOauthClientSecret() { return clientSecret; }
    };
  }
}
