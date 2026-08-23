package worldline.smoke.milkbucketsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Fills empty bucket 325 from a living cow via Packet7 button 0, then air-uses milk 335 back to 325. */
public final class MilkBucketSetSmoke {
  private static final RemoteItemStack EMPTY = new RemoteItemStack(325, 1, 0),
                                       MILK = new RemoteItemStack(335, 1, 0);
  private MilkBucketSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: MilkBucketSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("MilkBuck373") && user.length() <= 16,
        "milk-bucket-set identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, spawner;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 2, 325, 52}, new int[] {32, 48, 1, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 4
              && actor.awaitInventory().slot(38).item().equals(EMPTY)
              && actor.awaitHealth(20) == 20,
          "milk-bucket-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded milk-bucket-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
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
      actor.selectHeldSlot(3);
      spawner = place(actor, top, BlockFace.UP, 52);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
    } finally {
      actor.close();
      server.close();
    }
    Thread.sleep(1000L);
    B173SpawnerSeed.cow(workspace, spawner);
    server = B173DedicatedServer.animals(jar, workspace, port, seed, timeout, 3, true);
    actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() >= 1 && actor.awaitHealth(20) == 20,
          "milk-bucket-set reload inventory drift");
      RemoteMobSpawn spawn = actor.awaitMobSpawn(92);
      require(spawn.legacyType() == 92 && spawn.entityId() != actor.state().entityId(),
          "cow Packet24 identity drift");
      int bucket = find(actor.inventory(), 325);
      require(bucket >= 36, "empty bucket absent from hotbar");
      actor.selectHeldSlot(bucket - 36);
      approach(actor, spawn);
      bucket = find(actor.inventory(), 325);
      require(bucket >= 36, "empty bucket lost before milk");
      actor.selectHeldSlot(bucket - 36);
      B173BucketAccess.useOnMob(actor, spawn.entityId());
      final int[] selected = {bucket}, attempt = {0};
      RemoteInventorySlot held = worldline.test.WorldlineSmokeAwait.awaitCheckedEntity(actor,
          ()
              -> {
            int n = attempt[0]++;
            if (n % 5 == 4) {
              approach(actor, spawn);
              selected[0] = find(actor.inventory(), 325);
              if (selected[0] >= 36) {
                actor.selectHeldSlot(selected[0] - 36);
                B173BucketAccess.useOnMob(actor, spawn.entityId());
              }
            }
            if (selected[0] < 36)
              selected[0] = find(actor.inventory(), 335);
            return selected[0] >= 36 ? actor.inventory().slot(selected[0]) : null;
          },
          value
          -> value != null && !value.empty() && value.item().equals(MILK),
          "milk bucket fill", 40);
      bucket = selected[0];
      require(!held.empty() && held.item().equals(MILK) && actor.health() == 20,
          "milk fill drift held=" + (held.empty() ? "empty" : held.item())
              + " health=" + actor.health());
      actor.look(180F, 70F);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      actor.useSelectedItemInAir();
      final int drinkSlot = bucket >= 36 ? bucket : 37;
      held = worldline.test.WorldlineSmokeAwait.awaitEntity(actor,
          ()
              -> actor.inventory().slot(drinkSlot),
          value -> !value.empty() && value.item().equals(EMPTY), "milk bucket empty", 20);
      require(!held.empty() && held.item().equals(EMPTY) && actor.health() == 20,
          "milk drink drift held=" + (held.empty() ? "empty" : held.item())
              + " health=" + actor.health());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(
          reader.awaitHealth(20) == 20 && reader.awaitInventory().slot(bucket).item().equals(EMPTY),
          "persisted milk-bucket-set drift");
      String evidence = "column=" + column + ",floor=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,platform=7x7-48grass,spawner=52:0-cow,mob=type92,fill=packet7-button0,drink=packet15-dir255,held=325:1:0->335:1:0->325:1:0,health=20->20,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-7x7-grass-platform+cow-spawner52+empty-bucket325|cause=packet7-button0-bucket325+packet15-dir255-bucket335|wire=packet24-type92+packet103-milk335+packet103-bucket325|oracle=live-fill-drink-325/335+fresh-login-empty-325|"
          + evidence;
      System.out.println("WORLDLINE_M373_SET=" + evidence);
      System.out.println("WORLDLINE_M373_TRACE=" + trace);
      System.out.println("WORLDLINE_M373_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static int find(RemoteInventoryView view, int id) {
    for (int slot = 36; slot <= 44; slot++)
      if (!view.slot(slot).empty() && view.slot(slot).item().legacyId() == id)
        return slot;
    return -1;
  }
  private static void approach(B173WireClient a, RemoteMobSpawn spawn) {
    for (int step = 0; step < 8; step++) {
      PlayerPose here = a.moveAndObserve(0D, 0D, 0D, 1).resulting();
      double dx = spawn.x() - here.x(), dy = spawn.y() - here.y(), dz = spawn.z() - here.z(),
             dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
      if (dist <= 2.5D)
        return;
      double s = Math.min(1D, 4D / dist);
      a.moveAndObserve(dx * s, dy * s, dz * s, 8);
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
    throw new IllegalStateException("no deterministic milk-bucket-set foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
