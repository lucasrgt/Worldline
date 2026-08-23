package worldline.smoke.netherportaltravelb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.*;
import worldline.api.*;
import worldline.b173server.*;

/** Traverses one server-authored portal and decodes the destination Nether. */
public final class NetherPortalTraversalSmoke {
  private NetherPortalTraversalSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: NetherPortalTraversalSmoke server.jar workspace port seed username chunkX chunkZ portalTicks travelTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]),
        portalTicks = Integer.parseInt(a[7]), travelTicks = Integer.parseInt(a[8]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    BlockPosition anchor, bottom;
    PlayerPose pose, destination;
    RemoteChunkSnapshot nether;
    int column;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 49, 259}, new int[] {16, 14, 1}, new int[] {0, 0, 0});
      actor.connect();
      pose = actor.synchronizePose();
      require(
          actor.dimension() == 0 && actor.awaitDimension(0) == 0, "portal source dimension drift");
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
      for (int y = 1; y <= 3; y++)
        for (int x = 1; x <= 2; x++)
          require(active.blockAt(bottom.x() + x, bottom.y() + y, bottom.z()).legacyId() == 90,
              "portal precondition absent");
      pose = actor
                 .moveAndObserve(bottom.x() + 1.5D - pose.x(), bottom.y() + 1D - pose.y(),
                     bottom.z() + 0.5D - pose.z(), 1)
                 .resulting();
      require(actor.dimension() == 0, "dimension changed before portal residence");
      worldline.test.WorldlineSmokeAwait.observe(actor, travelTicks);
      require(actor.awaitDimension(-1) == -1, "official Packet9 transition absent");
      destination = actor.moveAndObserve(0D, 0D, 0D, 1).resulting();
      RemoteWorldView world = worldline.test.WorldlineSmokeAwait.observe(actor, 20);
      int dcx = floor(destination.x()) >> 4, dcz = floor(destination.z()) >> 4;
      nether = world.chunkAt(dcx, dcz);
      require(sky(nether) == 0 && count(nether, 87) > 0 && count(nether, 7) > 0,
          "portal destination is not decoded Nether terrain");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(
          server.player(user).dimension() == -1, "traversed player dimension was not persisted");
    } finally {
      actor.close();
      server.close();
    }
    int netherrack = count(nether, 87), bedrock = count(nether, 7), portal = count(nether, 90),
        obsidian = count(nether, 49);
    String evidence = "dimension=0->-1,column=" + column + ",source=" + bottom.x() + ":"
        + bottom.y() + ":" + bottom.z() + ",destinationChunk=" + (nether.observation().x() >> 4)
        + ":" + (nether.observation().z() >> 4) + ",sky=0,netherrack=" + netherrack
        + ",bedrock=" + bedrock + ",portal=" + portal + ",obsidian=" + obsidian;
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|profile=allow-nether-true|source=official-m132-portal|entry=packet11-inside-portal90|residence="
        + travelTicks
        + "ticks|transition=server-packet9-0-to-minus1|cache=old-dimension-invalidated|destination=packet13-pose+packet51-nether-chunk+generated-portal|oracle=stable-chunk-and-counts-not-dynamic-portal-coordinate|persistence=dimension-minus-one|"
        + evidence + "|disconnect=clean";
    System.out.println("WORLDLINE_M133_TRAVERSAL=" + evidence);
    System.out.println("WORLDLINE_M133_TRACE=" + trace);
    System.out.println("WORLDLINE_M133_SIGNATURE=" + sha(trace));
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
