package worldline.smoke.signtextsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places official standing sign 63 and wall sign 68 and proves both Packet130 texts after a restart. */
public final class SignTextSetSmoke {
  private SignTextSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: SignTextSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, standingCell, wallCell;
    int column;
    BlockState standing, wall;
    RemoteSignText standingText, wallText;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 323}, new int[] {32, 2}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "sign-text-set inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded sign-text-set fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      standingCell = BlockFace.UP.adjacent(top);
      wallCell = BlockFace.EAST.adjacent(top);
      require(
          initial.blockAt(local(wallCell.x(), cx), wallCell.y(), local(wallCell.z(), cz)).legacyId()
              == 0,
          "wall sign target was not initial air");
      actor.selectHeldSlot(1);
      actor.look(-90F, 0F);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      standing = waitBlock(actor, standingCell, 63);
      require(standing.legacyId() == 63 && standing.legacyId() != 68,
          "standing sign absent: " + standing);
      standingText = new RemoteSignText(standingCell, "Stand", "sign", "M350", "ok");
      worldline.test.WorldlineSmokeAwait.observe(actor, 10);
      B173SignAccess.update(actor, standingText);
      require(worldline.test.WorldlineSmokeAwait.observe(actor, 20)
                  .blockAt(standingCell.x(), standingCell.y(), standingCell.z())
                  .equals(standing),
          "live standing sign drift");
      actor.selectHeldSlot(1);
      actor.look(-90F, 0F);
      actor.useHeldItemOnBlock(top, BlockFace.EAST);
      wall = waitBlock(actor, wallCell, 68);
      require(wall.legacyId() == 68 && wall.legacyId() != 63, "wall sign absent: " + wall);
      wallText = new RemoteSignText(wallCell, "Wall", "text", "M350", "ok");
      worldline.test.WorldlineSmokeAwait.observe(actor, 10);
      B173SignAccess.update(actor, wallText);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      require(live.blockAt(standingCell.x(), standingCell.y(), standingCell.z()).equals(standing)
              && live.blockAt(wallCell.x(), wallCell.y(), wallCell.z()).equals(wall),
          "live sign-text-set drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after
                  .blockAt(
                      local(standingCell.x(), cx), standingCell.y(), local(standingCell.z(), cz))
                  .equals(standing)
              && after.blockAt(local(wallCell.x(), cx), wallCell.y(), local(wallCell.z(), cz))
                  .equals(wall),
          "persisted sign-text-set block drift");
      require(match(reader, standingText, wallText), "persisted Packet130 pair drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,standing=" + standingCell.x() + ":" + standingCell.y() + ":" + standingCell.z()
          + ":63:" + standing.metadata() + ",wall=" + wallCell.x() + ":" + wallCell.y() + ":"
          + wallCell.z() + ":68:" + wall.metadata()
          + ",text=Stand/sign/M350/ok+Wall/text/M350/ok,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+item323-block63+item323-block68|cause=packet15-item323-up+packet15-item323-east+packet130-ascii|wire=packet53-sign63:"
          + standing.metadata() + "+packet53-sign68:" + wall.metadata()
          + "+packet130-persist|oracle=fresh-login-packet130-63+packet130-68|" + evidence;
      System.out.println("WORLDLINE_M350_SET=" + evidence);
      System.out.println("WORLDLINE_M350_TRACE=" + trace);
      System.out.println("WORLDLINE_M350_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockState waitBlock(B173WireClient a, BlockPosition cell, int id)
      throws Exception {
    return worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
        a, cell, s -> s.legacyId() == id, "sign " + id, 40);
  }
  private static boolean match(B173WireClient r, RemoteSignText standing, RemoteSignText wall) {
    RemoteSignText s = null, w = null;
    for (int n = 0; n < 16 && (s == null || w == null); n++) {
      RemoteSignText v = B173SignAccess.poll(r);
      if (v == null)
        v = B173SignAccess.await(r);
      if (standing.equals(v))
        s = v;
      else if (wall.equals(v))
        w = v;
    }
    return s != null && w != null;
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
    throw new IllegalStateException("no deterministic sign-text-set foundation");
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
