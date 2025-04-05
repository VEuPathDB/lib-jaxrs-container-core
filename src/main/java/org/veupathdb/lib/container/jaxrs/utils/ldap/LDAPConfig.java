package org.veupathdb.lib.container.jaxrs.utils.ldap;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.veupathdb.lib.container.jaxrs.config.Options;

public class LDAPConfig {

  private static LDAPConfig instance;

  private Optional<List<String>> hosts;
  private Optional<String>       baseDn;

  public Optional<List<String>> hosts() {
    return hosts;
  }

  public Optional<String> baseDn() {
    return baseDn;
  }

  public static void initialize(final Options opts) {
    final var instance = new LDAPConfig();

    instance.hosts = opts.getLdapServers()
        .map(str -> Arrays.asList(str.split(",")));
    instance.baseDn = opts.getDbLookupBaseDn();

    LDAPConfig.instance = instance;
  }

  public static LDAPConfig getInstance() {
    return Objects.requireNonNull(instance);
  }
}
