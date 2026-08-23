package worldline.smoke.remainingnaturalspawnsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.TreeSet;
import worldline.api.*;
import worldline.b173server.*;

/** Observes night-time natural Packet24 hostiles without rewriting MobSpawner EntityId. */
public final class RemainingNaturalSpawnsSmoke {
  private RemainingNaturalSpawnsSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingNaturalSpawnsSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(240);
    require(seed == 17320110707L && user.equals("NatSpawn435") && user.length() <= 16,
        "remaining-natural-spawns identity drift");
    B173DedicatedServer server =
        B173DedicatedServer.monsters(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition top;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0}, new int[] {1},
          new int[] {32}, new int[] {0});
      actor.connect();
      actor.synchronizePose();
      require(
          actor.awaitInventory().occupiedSlots() == 1, "remaining-natural-spawns inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-natural-spawns fixture");
      }
      for (int lift = 0; lift < 2; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      server.setTime(14000L);
      TreeSet<Integer> types = new TreeSet<Integer>();
      int player = actor.state().entityId();
      while (types.size() < 2) {
        RemoteMobSpawn spawn = B173HostileAccess.next(actor);
        int type = spawn.legacyType();
        require(spawn.entityId() != player && (type == 50 || type == 51 || type == 52 || type == 54)
                && type != 90,
            "natural Packet24 identity drift");
        types.add(Integer.valueOf(type));
      }
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column
          + ",spawn-monsters=true,night=14000,spawners=absent,entityid=unmodified,family=50+51+52+54,observed>=2,clients=1,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|profile=spawn-monsters-true|entry=overworld-login+item1|fixture=dry-stone-column-no-spawner|cause=time-14000+natural-spawnercreature|wire=packet24-hostile-family-50-51-52-54|oracle=two-plus-natural-hostile-packet24-identities|"
          + evidence;
      System.out.println("WORLDLINE_M435_SET=" + evidence);
      System.out.println("WORLDLINE_M435_TRACE=" + trace);
      System.out.println("WORLDLINE_M435_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic remaining-natural-spawns foundation");
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long e = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < e) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b)
      v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
