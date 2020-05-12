package org.veupathdb.lib.container.jaxrs.health;

import org.veupathdb.lib.container.jaxrs.utils.net.Pinger;

abstract public class ExternalDependency extends AbstractDependency {

  protected Pinger pinger;

  protected ExternalDependency(String name) {
    super(name);
    pinger = new Pinger();
  }

  public void setPinger(Pinger pinger) {
    this.pinger = pinger;
  }
}
