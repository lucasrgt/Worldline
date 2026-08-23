package worldline.smoke.doublechestsetb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places two adjacent chests 54 so Packet100 opens a 54-slot large chest. */
public final class DoubleChestSetSmoke {
  private DoubleChestSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: DoubleChestSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, east, left, right;
    int column;
    BlockState leftState, rightState;
    RemoteContainerWindow window;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 54}, new int[] {32, 2}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "double-chest inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded double-chest fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      east = place(actor, top, BlockFace.EAST, 1);
      actor.moveAndObserve(0D, 0D, 1D, 2);
      actor.selectHeldSlot(1);
      left = place(actor, top, BlockFace.UP, 54);
      right = place(actor, east, BlockFace.UP, 54);
      leftState = new BlockState(54, 0);
      rightState = new BlockState(54, 0);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(left.x(), left.y(), left.z()).equals(leftState)
              && live.blockAt(right.x(), right.y(), right.z()).equals(rightState),
          "live adjacent chest 54:0 drift");
      window = actor.openChest(left, BlockFace.UP);
      require(large(window), "live Packet100 large-chest window absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(reader.awaitInventory().occupiedSlots() >= 1, "reload inventory drift");
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(east.x(), cx), east.y(), local(east.z(), cz))
                  .equals(new BlockState(1, 0))
              && after.blockAt(local(left.x(), cx), left.y(), local(left.z(), cz)).equals(leftState)
              && after.blockAt(local(right.x(), cx), right.y(), local(right.z(), cz))
                  .equals(rightState),
          "persisted adjacent chest 54:0 drift");
      reader.selectHeldSlot(1);
      RemoteContainerWindow again = reader.openChest(left, BlockFace.UP);
      require(large(again), "fresh-login Packet100 large-chest window absent");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0)
          + ",east=" + cell(east, 1, 0) + ",left=" + cell(left, 54, leftState.metadata())
          + ",right=" + cell(right, 54, rightState.metadata())
          + ",window=title=Large chest,owned=54,total=90,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+two-adjacent-chest54|cause=packet15-item54+packet15-item54+packet15-empty|wire=packet53-chest54:0+packet53-chest54:0+packet100-readUTF-owned54-title-Large-chest|oracle=double-chest-window54+both-cells+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M349_SET=" + evidence);
      System.out.println("WORLDLINE_M349_TRACE=" + trace);
      System.out.println("WORLDLINE_M349_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static boolean large(RemoteContainerWindow window) {
    RemoteWindowDescriptor d = window.descriptor();
    if (d.kind() != RemoteWindowKind.CHEST || !"Large chest".equals(d.title())
        || d.containerSlots() != 54 || window.inventory().size() != 90)
      return false;
    for (int i = 0; i < 54; i++)
      if (!window.inventory().slot(i).empty())
        return false;
    return true;
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
    throw new IllegalStateException("no deterministic double-chest foundation");
  }
  private static String cell(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
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
