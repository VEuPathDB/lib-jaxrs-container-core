package org.veupathdb.lib.container.jaxrs.providers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gusdb.oauth2.client.KeyStoreTrustManager;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.OAuthConfig;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;

import javax.net.ssl.TrustManager;
import java.nio.file.Paths;
import java.util.Optional;

public class OAuthProvider {

  private static final Logger LOG = LogManager.getLogger(OAuthProvider.class);

  public static final String DEFAULT_OAUTH_URL = "https://eupathdb.org/oauth";

  public static String getOAuthUrl() {
    return OptionsProvider.getOptions().getOAuthUrl().orElse(OAuthProvider.DEFAULT_OAUTH_URL);
  }

  public static OAuthClient getOAuthClient() {

    // if key store config is passed (probably mount of /etc/pki/java/cacerts), use it and optional pass phrase
    // otherwise use trust manager that trusts everyone
    Options options = OptionsProvider.getOptions();
    Optional<String> keyStoreFile = options.getKeyStoreFile().flatMap(f -> f.isBlank() ? Optional.empty() : Optional.of(f));
    TrustManager tm = keyStoreFile
        .map(file -> new KeyStoreTrustManager(Paths.get(file), options.getKeyStorePassPhrase().orElse("")))
        .orElse(new KeyStoreTrustManager());

    return new OAuthClient(tm);
  }

  public static OAuthConfig getOAuthConfig() {
    String oauthUrl = getOAuthUrl();

    Options options = OptionsProvider.getOptions();

    String clientId = options.getOAuthClientId().orElseThrow(() ->
        new InvalidConfigException("Client ID is required for this service"));
    String clientSecret = options.getOAuthClientSecret().orElseThrow(() ->
        new InvalidConfigException("Client secret is required for this service"));

    return OAuthConfig.build(oauthUrl, clientId, clientSecret);
  }
}
