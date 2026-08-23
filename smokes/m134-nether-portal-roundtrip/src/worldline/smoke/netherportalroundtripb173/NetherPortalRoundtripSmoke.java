package worldline.smoke.netherportalroundtripb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import worldline.api.*;
import worldline.b173server.*;

/** Traverses an official portal to the Nether and back to its source frame. */
public final class NetherPortalRoundtripSmoke {
  private NetherPortalRoundtripSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 10)
      throw new IllegalArgumentException(
          "usage: NetherPortalRoundtripSmoke server.jar workspace port seed username chunkX chunkZ portalTicks travelTicks cooldownTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]),
        portalTicks = Integer.parseInt(a[7]), travelTicks = Integer.parseInt(a[8]),
        cooldownTicks = Integer.parseInt(a[9]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition anchor, bottom;
    PlayerPose pose;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 49, 259}, new int[] {16, 14, 1}, new int[] {0, 0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(actor.dimension() == 0, "roundtrip source dimension drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      anchor = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(local(anchor.x(), cx), anchor.y() + 1, local(anchor.z(), cz))
              .legacyId())) {
        anchor = place(actor, anchor, BlockFace.UP, 1);
        pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
        column++;
        require(column <= 15, "water column exceeded fixture stack");
      }
      anchor = place(actor, anchor, BlockFace.UP, 1);
      pose = actor.moveAndObserve(0D, 1D, 0D, 1).resulting();
      column++;
      actor.selectHeldSlot(1);
      bottom = place(actor, anchor, BlockFace.UP, 49);
      BlockPosition p = bottom;
      for (int i = 0; i < 3; i++)
        p = place(actor, p, BlockFace.EAST, 49);
      BlockPosition left = bottom, right = p;
      for (int i = 0; i < 4; i++) {
        left = place(actor, left, BlockFace.UP, 49);
        right = place(actor, right, BlockFace.UP, 49);
      }
      p = left;
      for (int i = 0; i < 2; i++)
        p = place(actor, p, BlockFace.EAST, 49);
      actor.selectHeldSlot(2);
      actor.useHeldItemOnBlock(
          new BlockPosition(bottom.x() + 1, bottom.y(), bottom.z()), BlockFace.UP);
      RemoteWorldView active = worldline.test.WorldlineSmokeAwait.observe(actor, portalTicks);
      require(count(active.chunkAt(cx, cz), 90) == 6, "source portal precondition absent");
      pose = actor
                 .moveAndObserve(bottom.x() + 1.5D - pose.x(), bottom.y() + 1D - pose.y(),
                     bottom.z() + 0.5D - pose.z(), 1)
                 .resulting();
      worldline.test.WorldlineSmokeAwait.observe(actor, travelTicks);
      require(actor.awaitDimension(-1) == -1, "outbound Packet9 absent");
      pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
      RemoteWorldView netherWorld = worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      RemoteChunkSnapshot nether = netherWorld.chunkAt(floor(pose.x()) >> 4, floor(pose.z()) >> 4);
      Portal portal = find(netherWorld, pose);
      require(
          frame(netherWorld, portal) == 14 && sky(nether) == 0, "generated Nether portal drift");
      double tx = portal.minX + 0.5D, tz = portal.minZ + 0.5D, ty = portal.minY;
      if (portal.maxX > portal.minX)
        tz += 2.5D;
      else
        tx += 2.5D;
      pose = actor.moveAndObserve(tx - pose.x(), ty - pose.y(), tz - pose.z(), 5).resulting();
      require(actor.dimension() == -1, "left portal changed dimension early");
      worldline.test.WorldlineSmokeAwait.observe(actor, cooldownTicks);
      double enterX = portal.minX + 0.5D, enterZ = portal.minZ + 0.5D;
      pose = actor.moveAndObserve(enterX - pose.x(), portal.minY - pose.y(), enterZ - pose.z(), 1)
                 .resulting();
      worldline.test.WorldlineSmokeAwait.observe(actor, travelTicks);
      require(actor.awaitDimension(0) == 0, "return Packet9 absent");
      pose = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
      RemoteWorldView returned = worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      RemoteChunkSnapshot over = returned.chunkAt(floor(pose.x()) >> 4, floor(pose.z()) >> 4);
      Portal returnPortal = find(returned, pose);
      require(
          sky(over) > 0 && frame(returned, returnPortal) == 14, "Overworld portal return drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(server.player(user).dimension() == 0, "roundtrip player dimension was not persisted");
    } finally {
      actor.close();
      server.close();
    }
    String evidence = "dimensions=0->-1->0,column=" + column + ",source=" + bottom.x() + ":"
        + bottom.y() + ":" + bottom.z()
        + ",netherPortal=6:14,overworldPortal=6:14,cooldown=" + cooldownTicks;
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|profile=allow-nether-true|source=official-m132-portal|outbound=packet9-0-to-minus1|nether=discover-generated-portal+leave+cooldown|return=packet9-minus1-to-0|cache=invalidated-each-transition|observation=nether-and-overworld-packet51+portal14x6|oracle=portal-selection-coordinate-dynamic|persistence=dimension-zero|"
        + evidence + "|disconnect=clean";
    System.out.println("WORLDLINE_M134_ROUNDTRIP=" + evidence);
    System.out.println("WORLDLINE_M134_TRACE=" + trace);
    System.out.println("WORLDLINE_M134_SIGNATURE=" + sha(trace));
  }
  private static Portal find(RemoteWorldView w, PlayerPose pose) {
    int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, minY = 999, maxY = -1,
        minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE, n = 0;
    for (RemoteChunkSnapshot q : w.chunks()) {
      int bx = q.observation().x(), bz = q.observation().z();
      for (int x = 0; x < 16; x++)
        for (int z = 0; z < 16; z++)
          for (int y = 0; y < 128; y++)
            if (q.blockAt(x, y, z).legacyId() == 90 && Math.abs(bx + x - pose.x()) < 8D
                && Math.abs(y - pose.y()) < 8D && Math.abs(bz + z - pose.z()) < 8D) {
              n++;
              minX = Math.min(minX, bx + x);
              maxX = Math.max(maxX, bx + x);
              minY = Math.min(minY, y);
              maxY = Math.max(maxY, y);
              minZ = Math.min(minZ, bz + z);
              maxZ = Math.max(maxZ, bz + z);
            }
    }
    require(n == 6 && maxY - minY == 2
            && ((maxX - minX == 1 && maxZ == minZ) || (maxZ - minZ == 1 && maxX == minX)),
        "near-pose portal geometry drift " + n);
    return new Portal(minX, maxX, minY, minZ, maxZ, n);
  }
  private static int frame(RemoteWorldView w, Portal p) {
    int n = 0;
    if (p.maxX > p.minX) {
      for (int x = p.minX - 1; x <= p.maxX + 1; x++) {
        if (w.blockAt(x, p.minY - 1, p.minZ).legacyId() == 49)
          n++;
        if (w.blockAt(x, p.minY + 3, p.minZ).legacyId() == 49)
          n++;
      }
      for (int y = p.minY; y <= p.minY + 2; y++) {
        if (w.blockAt(p.minX - 1, y, p.minZ).legacyId() == 49)
          n++;
        if (w.blockAt(p.maxX + 1, y, p.minZ).legacyId() == 49)
          n++;
      }
    } else {
      for (int z = p.minZ - 1; z <= p.maxZ + 1; z++) {
        if (w.blockAt(p.minX, p.minY - 1, z).legacyId() == 49)
          n++;
        if (w.blockAt(p.minX, p.minY + 3, z).legacyId() == 49)
          n++;
      }
      for (int y = p.minY; y <= p.minY + 2; y++) {
        if (w.blockAt(p.minX, y, p.minZ - 1).legacyId() == 49)
          n++;
        if (w.blockAt(p.minX, y, p.maxZ + 1).legacyId() == 49)
          n++;
      }
    }
    return n;
  }
  private static final class Portal {
    final int minX, maxX, minY, minZ, maxZ, count;
    Portal(int a, int b, int c, int d, int e, int n) {
      minX = a;
      maxX = b;
      minY = c;
      minZ = d;
      maxZ = e;
      count = n;
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 10; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic portal foundation");
  }
  private static int sky(RemoteChunkSnapshot q) {
    int n = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (q.skyLightAt(x, y, z) > 0)
            n++;
    return n;
  }
  private static int count(RemoteChunkSnapshot q, int id) {
    int n = 0;
    for (int x = 0; x < 16; x++)
      for (int z = 0; z < 16; z++)
        for (int y = 0; y < 128; y++)
          if (q.blockAt(x, y, z).legacyId() == id)
            n++;
    return n;
  }
  private static int floor(double v) {
    return (int) Math.floor(v);
  }
  private static String sha(String s) throws Exception {
    return hex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
  }
  private static String hex(byte[] b) {
    StringBuilder s = new StringBuilder();
    for (byte v : b)
      s.append(String.format("%02x", v & 255));
    return s.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
