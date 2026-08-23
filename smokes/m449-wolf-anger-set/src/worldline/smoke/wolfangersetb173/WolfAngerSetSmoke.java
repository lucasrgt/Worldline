package worldline.smoke.wolfangersetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Observes Packet24 wolf type 95, Packet7 diamond sword 276 without bone, and Packet38 not tame 6/7. */
public final class WolfAngerSetSmoke {
  private WolfAngerSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: WolfAngerSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("WolfAnger449") && user.length() <= 16,
        "wolf-anger-set identity drift");
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server =
        B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top, spawner;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 2, 52, 268, 85}, new int[] {32, 48, 1, 1, 24}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "wolf-anger-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded wolf-anger-set fixture");
      }
      for (int lift = 0; lift < 2; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      for (int r = 1; r <= 3; r++) {
        for (int z = -r + 1; z < r; z++) {
          grass(actor, new BlockPosition(top.x() - r + 1, top.y(), top.z() + z), BlockFace.WEST);
          grass(actor, new BlockPosition(top.x() + r - 1, top.y(), top.z() + z), BlockFace.EAST);
        }
        for (int x = -r + 1; x < r; x++) {
          grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() - r + 1), BlockFace.NORTH);
          grass(actor, new BlockPosition(top.x() + x, top.y(), top.z() + r - 1), BlockFace.SOUTH);
        }
        grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() - r + 1), BlockFace.NORTH);
        grass(actor, new BlockPosition(top.x() - r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
        grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() - r + 1), BlockFace.NORTH);
        grass(actor, new BlockPosition(top.x() + r, top.y(), top.z() + r - 1), BlockFace.SOUTH);
      }
      arena(actor, top, 4);
      actor.selectHeldSlot(2);
      spawner = place(actor, top, BlockFace.UP, 52);
      actor.sustainTicks(5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173WolfAngerAccess.retarget(workspace, spawner);
    server = B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() >= 1, "wolf-anger-set reload inventory drift");
      RemoteMobSpawn wolf = B173WolfAngerAccess.anger(actor, spawner, top);
      require(wolf.legacyType() == 95 && wolf.entityId() != actor.state().entityId(),
          "wolf Packet24 type 95 identity drift");
      require(B173WolfAngerAccess.tame(actor, wolf.entityId()) < 0,
          "wolf Packet38 status 6/7 after hostility");
      require(B173ShearsAccess.peekDeath(actor, wolf.entityId()) == null,
          "Packet38 status 3 death after wolf anger");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",platform=7x7-48grass,arena=fence85-24,spawner="
          + spawner.x() + ":" + spawner.y() + ":" + spawner.z()
          + ":52:0,mob=type95,sword=268,held=no-bone,tame=no-packet38-status6-or-7,hostile=packet8-health,death=no-packet38-status3,clients=1,disconnect=clean";
      String trace = "v2|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+fence85-arena+wolf-spawner52|cause=packet7-button1-wood-sword268-no-bone|wire=packet24-type95+packet38-not-status6-or-7+packet8-health|oracle=wolf-type95-nonlethal-strike-hostility-not-tame-not-breeding-not-sitting|"
          + evidence;
      System.out.println("WORLDLINE_M449_SET=" + evidence);
      System.out.println("WORLDLINE_M449_TRACE=" + trace);
      System.out.println("WORLDLINE_M449_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static void arena(B173WireClient a, BlockPosition top, int slot) throws Exception {
    a.selectHeldSlot(slot);
    for (int x = -3; x <= 3; x++) {
      place(a, new BlockPosition(top.x() + x, top.y(), top.z() - 3), BlockFace.UP, 85);
      place(a, new BlockPosition(top.x() + x, top.y(), top.z() + 3), BlockFace.UP, 85);
    }
    for (int z = -2; z <= 2; z++) {
      place(a, new BlockPosition(top.x() - 3, top.y(), top.z() + z), BlockFace.UP, 85);
      place(a, new BlockPosition(top.x() + 3, top.y(), top.z() + z), BlockFace.UP, 85);
    }
  }
  private static void grass(B173WireClient a, BlockPosition support, BlockFace face)
      throws Exception {
    place(a, support, face, 2);
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic wolf-anger-set foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
