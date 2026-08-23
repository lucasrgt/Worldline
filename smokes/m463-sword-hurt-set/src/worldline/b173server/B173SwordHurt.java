package worldline.b173server;

import java.io.IOException;
import worldline.api.PlayerPose;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteMobDeath;
import worldline.api.RemoteMobMovement;
import worldline.api.RemoteMobSpawn;

/** One Packet7 diamond-sword 276 hit, Packet38 status 2 wait, and a non-blocking death peek. */
public final class B173SwordHurt {
  private B173SwordHurt() {
  }

  public static RemoteMobDeath peekDeath(B173WireClient client, int entity) {
    return B173ShearsAccess.peekDeath(client, entity);
  }

  public static boolean peekHurt(B173WireClient client, int entity) {
    if (entity < 0)
      throw new IllegalArgumentException("invalid expected mob entity");
    return client.channel().inbound().mobs().peekHurt(entity);
  }

  public static void awaitHurt(B173WireClient client, int entity) {
    if (entity < 0)
      throw new IllegalArgumentException("invalid expected mob entity");
    B173PlayInbound inbound = client.channel().inbound();
    Thread pulse = inbound.pulse();
    long deadline = System.nanoTime() + inbound.timeoutNanos();
    try {
      for (int count = 0; count < 8192 && System.nanoTime() < deadline; count++) {
        if (inbound.mobs().peekHurt(entity))
          return;
        try {
          inbound.pumpOne();
        } catch (IOException error) {
          throw new IllegalStateException("mob Packet38 status 2 absent", error);
        }
        if (inbound.mobs().peekHurt(entity))
          return;
      }
      throw new IllegalStateException("mob Packet38 status 2 absent before deadline");
    } finally {
      pulse.interrupt();
    }
  }

  public static void station(B173WireClient client, double x, double y, double z) {
    step(client, x, y, z, 1.5D);
  }

  public static void oneHit(B173WireClient client, RemoteMobSpawn spawn, boolean creeper) {
    int entity = spawn.entityId();
    double x = spawn.x(), y = spawn.y(), z = spawn.z();
    if (creeper)
      far(client, x, y, z);
    else
      close(client, x, y + 1.0D, z - 1.5D);
    for (int n = 0; n < 16 && !peekHurt(client, entity); n++) {
      if (peekDeath(client, entity) != null)
        throw new IllegalStateException("Packet38 status 3 before sword hurt");
      if (creeper) {
        reach(client, x, y, z);
        strike(client, entity);
        far(client, x, y, z);
      } else {
        close(client, x, y + 1.0D, z - 1.5D);
        strike(client, entity);
      }
      client.sustainTicks(20);
      if (peekDeath(client, entity) != null)
        throw new IllegalStateException("Packet38 status 3 after first sword hit");
      if (!peekHurt(client, entity)) {
        RemoteMobMovement move;
        while ((move = client.channel().inbound().mobs().takeMovement(entity)) != null) {
          x = move.toX();
          y = move.toY();
          z = move.toZ();
        }
      }
    }
    if (!peekHurt(client, entity))
      throw new IllegalStateException("Packet38 status 2 absent after bounded sword hits");
    awaitHurt(client, entity);
    if (peekDeath(client, entity) != null)
      throw new IllegalStateException("peekDeath after first Packet38 status 2");
    if (creeper)
      far(client, x, y, z);
  }

  private static void strike(B173WireClient client, int entity) {
    heal(client);
    int sword = find(client.inventory(), 276);
    if (sword < 36)
      throw new IllegalStateException("diamond sword lost");
    client.selectHeldSlot(sword - 36);
    client.attackMob(entity);
  }

  private static void close(B173WireClient client, double x, double y, double z) {
    step(client, x, y, z, 2.5D);
  }

  private static void reach(B173WireClient client, double x, double y, double z) {
    step(client, x, y, z - 5.2D, 1.5D);
  }

  private static void far(B173WireClient client, double x, double y, double z) {
    step(client, x, y, z - 8.5D, 1.5D);
  }

  private static void step(B173WireClient client, double x, double y, double z, double reach) {
    for (int n = 0; n < 16; n++) {
      heal(client);
      PlayerPose here = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = x - here.x(), dy = y - here.y(), dz = z - here.z();
      double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist <= reach)
        return;
      double s = Math.min(1D, 9.0D / dist);
      client.moveAndObserve(dx * s, dy * s, dz * s, 2);
    }
  }

  private static void heal(B173WireClient client) {
    int health = client.health();
    if (health == 0)
      throw new IllegalStateException("actor died during sword hurt");
    if (health >= 20)
      return;
    int food = find(client.inventory(), 322);
    if (food < 36)
      food = find(client.inventory(), 320);
    if (food < 36)
      return;
    client.selectHeldSlot(food - 36);
    client.useSelectedItemInAir();
    client.sustainTicks(5);
  }

  private static int find(RemoteInventoryView view, int id) {
    for (int slot = 36; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    return -1;
  }
}
