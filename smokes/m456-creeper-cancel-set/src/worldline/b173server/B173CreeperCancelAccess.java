package worldline.b173server;

import worldline.api.PlayerPose;
import worldline.api.RemoteExplosion;

/** Smoke-local Packet40 creeper fuse wait, 9-block steps, and Packet60 peek. */
public final class B173CreeperCancelAccess {
  public static final int TYPE = 50, IGNITE = 1, CANCEL = -1, WAIT = 45;

  private B173CreeperCancelAccess() {
  }

  public static worldline.api.RemoteMobSpawn near(
      B173WireClient client, worldline.api.BlockPosition pad) {
    for (int n = 0; n < 32; n++) {
      worldline.api.RemoteMobSpawn spawn = client.awaitMobSpawn(TYPE);
      double dx = spawn.x() - (pad.x() + 0.5D), dz = spawn.z() - (pad.z() + 0.5D);
      if (dx * dx + dz * dz <= 100D && Math.abs(spawn.y() - pad.y()) <= 6D
          && spawn.entityId() != client.state().entityId())
        return spawn;
    }
    throw new IllegalStateException("nearby creeper type 50 absent");
  }

  public static PlayerPose stand(B173WireClient client, worldline.api.BlockPosition top) {
    PlayerPose pose = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
    for (int n = 0; n < 32 && pose.y() > top.y() + 1.01D; n++)
      pose = client.moveAndObserve(0D, -1D, 0D, 1).resulting();
    if (Math.abs(pose.x() - (top.x() + 0.5D)) > 3D || Math.abs(pose.z() - (top.z() + 0.5D)) > 3D
        || pose.y() > top.y() + 2.5D)
      throw new IllegalStateException(
          "actor missed creeper pad at " + pose.x() + "," + pose.y() + "," + pose.z());
    return pose;
  }

  public static void fleeWest(B173WireClient client) {
    PlayerPose before = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
    boolean clear = false;
    for (int n = 0; n < 4; n++) {
      client.moveAndObserve(-9D, 0D, 0D, 2);
      PlayerPose after = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
      if (before.x() - after.x() >= 7.5D)
        clear = true;
    }
    PlayerPose after = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
    if (!clear)
      throw new IllegalStateException("creeper-cancel flee stayed in range before=" + before.x()
          + "," + before.y() + "," + before.z() + " after=" + after.x() + "," + after.y() + ","
          + after.z());
  }

  public static int awaitState(B173WireClient client, int entity, int expected) {
    if (expected != IGNITE && expected != CANCEL)
      throw new IllegalArgumentException("invalid creeper fuse state");
    for (int n = 0; n < 200; n++) {
      int state = client.channel().inbound().mobs().takeFuse(entity, expected);
      if (state == expected)
        return state;
      client.sustainTicks(1);
      state = client.channel().inbound().mobs().takeFuse(entity, expected);
      if (state == expected)
        return state;
    }
    PlayerPose here = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
    throw new IllegalStateException("creeper Packet40 index 16 state " + expected
        + " absent queued=" + client.channel().inbound().mobs().fuseQueued(entity) + " explosion="
        + peekExplosion(client) + " pose=" + here.x() + "," + here.y() + "," + here.z());
  }

  public static RemoteExplosion peekExplosion(B173WireClient client) {
    return client.channel().inbound().peekExplosion();
  }
}
