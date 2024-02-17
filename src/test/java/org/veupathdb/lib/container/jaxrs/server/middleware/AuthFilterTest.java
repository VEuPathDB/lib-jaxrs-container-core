package org.veupathdb.lib.container.jaxrs.server.middleware;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veupathdb.lib.container.jaxrs.config.InvalidConfigException;
import org.veupathdb.lib.container.jaxrs.config.Options;

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
}
