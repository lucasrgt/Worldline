package worldline.smoke.remainingtntplaceb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places two TNT 46 cells, flint-and-steel 259 primes one, and freezes the Packet60 chain. */
public final class RemainingTntPlaceSmoke {
  private RemainingTntPlaceSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: RemainingTntPlaceSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks fuseTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    int fixtureTicks = Integer.parseInt(a[7]), fuseTicks = Integer.parseInt(a[8]);
    require(seed == 17320110707L && user.equals("TntChain417") && user.length() <= 16,
        "remaining-tnt-place identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, tnt1, tnt2;
    int column;
    RemoteObjectSpawn primed, chained;
    RemoteExplosion first, second;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 46, 259}, new int[] {32, 2, 1}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "remaining-tnt-place inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-tnt-place fixture");
      }
      for (int lift = 0; lift < 6; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      BlockPosition far = place(actor, place(actor, top, BlockFace.EAST, 1), BlockFace.EAST, 1);
      actor.selectHeldSlot(1);
      tnt1 = place(actor, top, BlockFace.UP, 46);
      tnt2 = place(actor, far, BlockFace.UP, 46);
      RemoteWorldView before = worldline.test.WorldlineSmokeAwait.observe(actor, fixtureTicks);
      require(before.blockAt(top.x(), top.y(), top.z()).equals(new BlockState(1, 0))
              && before.blockAt(tnt1.x(), tnt1.y(), tnt1.z()).equals(new BlockState(46, 0))
              && before.blockAt(tnt2.x(), tnt2.y(), tnt2.z()).equals(new BlockState(46, 0))
              && tnt2.x() == tnt1.x() + 2 && tnt2.y() == tnt1.y() && tnt2.z() == tnt1.z(),
          "two TNT 46:0 baseline drift");
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(tnt1, BlockFace.UP);
      primed = actor.awaitObjectSpawn(50);
      require(primed.type() == 50 && primed.entityId() != actor.state().entityId()
              && primed.throwerId() == 0,
          "Packet23 type 50 first primed TNT drift");
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 1);
      int secondId = live.blockAt(tnt2.x(), tnt2.y(), tnt2.z()).legacyId();
      require(secondId == 46,
          "second TNT 46 cell gone before chain id=" + secondId
              + " first=" + live.blockAt(tnt1.x(), tnt1.y(), tnt1.z()).legacyId());
      actor.moveAndObserve(10D, 0D, 0D, 4);
      first = actor.awaitExplosion();
      require(first.strength() == 4F && Math.abs(first.x() - (tnt1.x() + 0.5D)) < 4D
              && Math.abs(first.y() - (tnt1.y() + 0.5D)) < 6D
              && Math.abs(first.z() - (tnt1.z() + 0.5D)) < 4D,
          "first Packet60 center/strength drift");
      chained = actor.awaitObjectSpawn(50);
      require(chained.type() == 50 && chained.entityId() != primed.entityId()
              && chained.entityId() != actor.state().entityId() && chained.throwerId() == 0,
          "Packet23 type 50 chained TNT drift");
      actor.awaitBlock(tnt2, new BlockState(0, 0));
      second = actor.awaitExplosion();
      require(second.strength() == 4F && first.strength() == 4F && !second.equals(first),
          "second Packet60 strength/identity drift " + second.strength());
      RemoteWorldView after = worldline.test.WorldlineSmokeAwait.observe(actor, 1);
      require(after.blockAt(tnt1.x(), tnt1.y(), tnt1.z()).equals(new BlockState(0, 0))
              && after.blockAt(tnt2.x(), tnt2.y(), tnt2.z()).equals(new BlockState(0, 0)),
          "remaining-tnt-place chain crater drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteWorldView persisted = reader.awaitRemoteChunk(cx, cz);
      require(persisted.blockAt(tnt1.x(), tnt1.y(), tnt1.z()).equals(new BlockState(0, 0))
              && persisted.blockAt(tnt2.x(), tnt2.y(), tnt2.z()).equals(new BlockState(0, 0)),
          "fresh chain crater persistence drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,tnt1=" + tnt1.x() + ":" + tnt1.y() + ":" + tnt1.z()
          + ":46:0->0:0,tnt2=" + tnt2.x() + ":" + tnt2.y() + ":" + tnt2.z()
          + ":46:0->0:0,flint=259,packet23=50+50,packet60=strength4,chain=true,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+tnt46+tnt46+flint259|cause=packet15-item46+packet15-item46+packet15-item259-prime-one|fuse="
          + fuseTicks
          + "ticks|wire=packet60-strength4+packet60-strength4+packet23-type50-chain|oracle=live-two-tnt-chain+crater-air+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M417_SET=" + evidence);
      System.out.println("WORLDLINE_M417_TRACE=" + trace);
      System.out.println("WORLDLINE_M417_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
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
    throw new IllegalStateException("no deterministic remaining-tnt-place foundation");
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
