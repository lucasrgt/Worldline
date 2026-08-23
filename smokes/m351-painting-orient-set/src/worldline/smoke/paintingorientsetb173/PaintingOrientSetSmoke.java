package worldline.smoke.paintingorientsetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places painting item 321 on two stone-wall faces and correlates Packet25. */
public final class PaintingOrientSetSmoke {
  private PaintingOrientSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 8)
      throw new IllegalArgumentException(
          "usage: PaintingOrientSetSmoke server.jar workspace port seed actor observer chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String actorName = a[4], observerName = a[5];
    int cx = Integer.parseInt(a[6]), cz = Integer.parseInt(a[7]);
    Duration timeout = Duration.ofSeconds(90);
    require(
        actorName.length() <= 16 && observerName.length() <= 16 && !actorName.equals(observerName),
        "username drift");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, actorName, timeout),
                   observer = new B173WireClient("127.0.0.1", port, observerName, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 321}, new int[] {32, 2}, new int[] {0, 0});
      B173PlayerSeed.write(workspace, observerName, 4.5D, 80D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "painting inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(initial, cx, cz);
      int column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded painting fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      BlockPosition support = place(actor, top, BlockFace.EAST, 1),
                    south = place(actor, support, BlockFace.UP, 1),
                    north = place(actor, south, BlockFace.SOUTH, 1),
                    southTop = place(actor, south, BlockFace.UP, 1);
      place(actor, north, BlockFace.UP, 1);
      BlockPosition westSupport = place(actor, top, BlockFace.WEST, 1),
                    westNorth = place(actor, westSupport, BlockFace.NORTH, 1),
                    eastClick = place(actor, westNorth, BlockFace.UP, 1),
                    eastFar = place(actor, eastClick, BlockFace.NORTH, 1),
                    eastTop = place(actor, eastClick, BlockFace.UP, 1);
      place(actor, eastFar, BlockFace.UP, 1);
      observer.connect();
      observer.synchronizePose();
      observer.awaitRemoteChunk(cx, cz);
      actor.selectHeldSlot(1);
      actor.look(-90F, 0F);
      actor.useHeldItemOnBlock(south, BlockFace.WEST);
      RemotePaintingSpawn west = B173PaintingAccess.await(actor),
                          westPeer = B173PaintingAccess.await(observer);
      require(west.equals(westPeer) && west.entityId() != actor.state().entityId()
              && west.entityId() != observer.state().entityId() && west.packet() == 25,
          "peer west painting spawn drift");
      actor.look(90F, 0F);
      actor.useHeldItemOnBlock(eastClick, BlockFace.EAST);
      RemotePaintingSpawn east = B173PaintingAccess.await(actor),
                          eastPeer = B173PaintingAccess.await(observer);
      require(east.equals(eastPeer) && east.entityId() != west.entityId()
              && east.entityId() != actor.state().entityId()
              && east.entityId() != observer.state().entityId() && east.packet() == 25
              && east.direction() != west.direction(),
          "peer east painting spawn drift");
      actor.close();
      observer.close();
      awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + column + ",west-wall=" + south.x() + ":" + south.y() + ":"
          + south.z() + "-" + southTop.x() + ":" + southTop.y() + ":" + north.z() + ":1:0,west="
          + west.x() + ":" + west.y() + ":" + west.z() + ":dir" + west.direction() + ",east-wall="
          + eastClick.x() + ":" + eastClick.y() + ":" + eastClick.z() + "-" + eastTop.x() + ":"
          + eastTop.y() + ":" + eastFar.z() + ":1:0,east=" + east.x() + ":" + east.y() + ":"
          + east.z() + ":dir" + east.direction() + ",packet25+packet25,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-2x2-stone-walls-west+east|cause=packet15-item321-west+packet15-item321-east|wire=packet25+packet25|oracle=two-peer-identical-painting-spawns-multi-facing|"
          + evidence;
      System.out.println("WORLDLINE_M351_SET=" + evidence);
      System.out.println("WORLDLINE_M351_TRACE=" + trace);
      System.out.println("WORLDLINE_M351_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      observer.close();
      server.close();
    }
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic painting foundation");
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
