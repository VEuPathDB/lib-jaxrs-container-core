package org.veupathdb.lib.container.jaxrs.server.middleware;

import io.jsonwebtoken.Claims;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.glassfish.jersey.client.ClientConfig;
import org.gusdb.oauth2.client.KeyStoreTrustManager;
import org.gusdb.oauth2.client.OAuthClient;
import org.gusdb.oauth2.client.ValidatedToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthFilterTest {

  @Mock private Options options;
  @Mock private ContainerRequestContext requestContext;
  @Mock private UriInfo uriInfo;

  @Test
  public void testConstructMissingAuthKey() {
    when(options.getAuthSecretKey()).thenReturn(Optional.empty());
    Assertions.assertThrows(InvalidConfigException.class, () -> new AuthFilter(options));
  }

  @Test
  public void testConstructBlankAuthKey() {
    when(options.getAuthSecretKey()).thenReturn(Optional.of(""));
    Assertions.assertThrows(InvalidConfigException.class, () -> new AuthFilter(options));
  }

  //@Test Not a unit test
  public void testBearerTokenAttachment() {
    String rawToken = "";

    checkMessageHeader(HttpHeaders.AUTHORIZATION, rawToken);

    ValidatedToken token = new ValidatedToken() {
      @Override
      public TokenType getTokenType() {
        return TokenType.BEARER;
      }
      @Override
      public String getTokenValue() {
        return rawToken;
      }
      @Override
      public Claims getTokenContents() {
        return null;
      }
    };
    try {
      Response response = ClientBuilder.newBuilder()
          .withConfig(new ClientConfig())
          .sslContext(createSslContext())
          .build()
          .target("http://eupathdb.org/oauth/user")
          .request(MediaType.APPLICATION_JSON)
          .header(HttpHeaders.AUTHORIZATION, OAuthClient.getAuthorizationHeaderValue(token))
          .get();
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (KeyManagementException e) {
      e.printStackTrace();
    }
  }

  private static SSLContext createSslContext() throws NoSuchAlgorithmException, KeyManagementException {
    SSLContext sslContext = SSLContext.getInstance("SSL");
    sslContext.init(null, new TrustManager[]{ new KeyStoreTrustManager()}, null);
    return sslContext;
  }

  private void checkMessageHeader(String key, String value) {
    char LF = '\n';
    int index = key.indexOf(LF);
    int index1 = key.indexOf(':');
    if (index != -1 || index1 != -1) {
      throw new IllegalArgumentException(
          "Illegal character(s) in message header field: " + key);
    }
    else {
      if (value == null) {
        return;
      }

      index = value.indexOf(LF);
      while (index != -1) {
        index++;
        if (index < value.length()) {
          char c = value.charAt(index);
          if ((c==' ') || (c=='\t')) {
            // ok, check the next occurrence
            index = value.indexOf(LF, index);
            continue;
          }
        }
        throw new IllegalArgumentException(
            "Illegal character(s) in message header value: " + value);
      }
    }
  }
}
