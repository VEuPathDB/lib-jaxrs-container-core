package org.veupathdb.lib.container.jaxrs.health;

import org.junit.jupiter.api.Test;

import java.net.ServerSocket;

import org.veupathdb.lib.container.jaxrs.utils.net.Pinger;

import static org.junit.jupiter.api.Assertions.*;

class PingerTest {

  @Test
  void isReachable() throws Exception{
    try (var sock = new ServerSocket(10101)) {
      assertTrue(new Pinger().isReachable("localhost", 10101));
      assertFalse(new Pinger().isReachable("localhost", 1));
    }
  }
}
