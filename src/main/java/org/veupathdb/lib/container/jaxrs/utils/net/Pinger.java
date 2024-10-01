package org.veupathdb.lib.container.jaxrs.utils.net;

import org.slf4j.Logger;
import org.veupathdb.lib.container.jaxrs.providers.LogProvider;

import java.io.IOException;
import java.net.Socket;

public class Pinger {

  private final Logger log = LogProvider.logger(Pinger.class);

  public boolean isReachable(String addr, int port) {
    log.debug("Pinging {}:{}", addr, port);

    try (var sock = new Socket(addr, port)) {
      sock.setSoTimeout(3000);
      sock.setTcpNoDelay(true);
      sock.getOutputStream().flush();
      return true;
    } catch (IOException e) {
      log.info("Ping failed for {}:{}", addr, port);
      log.debug("Ping Exception:", e);
      return false;
    }
  }
}
