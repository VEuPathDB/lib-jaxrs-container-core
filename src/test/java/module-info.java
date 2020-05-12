module epvm.lib.container.jaxrs.core {
  requires transitive com.fasterxml.jackson.annotation;
  requires transitive java.ws.rs;
  requires transitive org.apache.logging.log4j;

  requires java.annotation;
  requires java.management;
  requires java.logging;

  requires jersey.server;
  requires jersey.media.json.jackson;
  requires simpleclient;
  requires simpleclient.common;
  requires info.picocli;
  requires org.apache.logging.log4j.core;
  requires friendly.id;
  requires grizzly.http.server;
  requires jersey.container.grizzly2.http;
  requires fgputil.accountdb;
  requires fgputil.util;

  requires org.junit.jupiter.api;
  requires org.mockito;

  exports org.veupathdb.lib.container.jaxrs.config;
  exports org.veupathdb.lib.container.jaxrs.health;
  exports org.veupathdb.lib.container.jaxrs.providers;
  exports org.veupathdb.lib.container.jaxrs.server;
}
