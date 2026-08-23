package worldline.b173server;

import worldline.api.RemoteMobMovement;

/** Exposes a non-blocking smoke-only read of adapter-owned mob movement. */
public final class B173SpiderClimbAccess {
  private B173SpiderClimbAccess() {
  }

  public static RemoteMobMovement poll(B173WireClient client, int entity) {
    if (client == null || entity < 1)
      throw new IllegalArgumentException("invalid spider movement probe");
    return client.channel().inbound().mobs().takeMovement(entity);
  }
}
