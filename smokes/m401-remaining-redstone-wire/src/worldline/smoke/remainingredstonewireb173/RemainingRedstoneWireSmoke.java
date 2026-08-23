package worldline.smoke.remainingredstonewireb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places remaining redstone-wire 55 as cross, line, and elbow connection shapes in one SET. */
public final class RemainingRedstoneWireSmoke {
  private RemainingRedstoneWireSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingRedstoneWireSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    require(seed == 17320110707L && user.equals("DustWire401") && user.length() <= 16,
        "remaining-redstone-wire identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, north, south, east, west, south2, lineMid, lineWest, lineEast, east2, elbow,
        elbowEast, elbowSouth, crossC, lineC, elbowC;
    int column;
    BlockState dust = new BlockState(55, 0), stone = new BlockState(1, 0);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1},
          new int[] {1, 331}, new int[] {64, 16}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(
          actor.awaitInventory().occupiedSlots() == 2, "remaining-redstone-wire inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded remaining-redstone-wire fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      north = place(actor, top, BlockFace.NORTH, 1);
      south = place(actor, top, BlockFace.SOUTH, 1);
      east = place(actor, top, BlockFace.EAST, 1);
      west = place(actor, top, BlockFace.WEST, 1);
      south2 = place(actor, south, BlockFace.SOUTH, 1);
      lineMid = place(actor, south2, BlockFace.SOUTH, 1);
      lineWest = place(actor, lineMid, BlockFace.WEST, 1);
      lineEast = place(actor, lineMid, BlockFace.EAST, 1);
      east2 = place(actor, east, BlockFace.EAST, 1);
      elbow = place(actor, east2, BlockFace.EAST, 1);
      elbowEast = place(actor, elbow, BlockFace.EAST, 1);
      elbowSouth = place(actor, elbow, BlockFace.SOUTH, 1);
      actor.selectHeldSlot(1);
      crossC = dust(actor, top);
      dust(actor, north);
      dust(actor, south);
      dust(actor, east);
      dust(actor, west);
      lineC = dust(actor, lineMid);
      dust(actor, lineWest);
      dust(actor, lineEast);
      elbowC = dust(actor, elbow);
      dust(actor, elbowEast);
      dust(actor, elbowSouth);
      RemoteWorldView live = worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      String crossL = links(live, crossC), lineL = links(live, lineC), elbowL = links(live, elbowC);
      require(live.blockAt(crossC.x(), crossC.y(), crossC.z()).equals(dust)
              && live.blockAt(lineC.x(), lineC.y(), lineC.z()).equals(dust)
              && live.blockAt(elbowC.x(), elbowC.y(), elbowC.z()).equals(dust)
              && crossL.equals("nsew") && lineL.equals("ew") && elbowL.equals("se")
              && !crossL.equals(lineL) && !lineL.equals(elbowL),
          "live remaining-redstone-wire shape drift: " + crossL + "/" + lineL + "/" + elbowL);
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteWorldView after = reader.awaitRemoteChunk(cx, cz);
      require(after.blockAt(top.x(), top.y(), top.z()).equals(stone)
              && after.blockAt(crossC.x(), crossC.y(), crossC.z()).equals(dust)
              && after.blockAt(lineC.x(), lineC.y(), lineC.z()).equals(dust)
              && after.blockAt(elbowC.x(), elbowC.y(), elbowC.z()).equals(dust)
              && links(after, crossC).equals("nsew") && links(after, lineC).equals("ew")
              && links(after, elbowC).equals("se"),
          "persisted remaining-redstone-wire shape drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0)
          + ",cross=" + cell(crossC, 55, 0) + ":" + crossL + ",line=" + cell(lineC, 55, 0) + ":"
          + lineL + ",elbow=" + cell(elbowC, 55, 0) + ":" + elbowL
          + ",persisted=55:0+55:0+55:0,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+wire55-cross+wire55-line+wire55-elbow|cause=packet15-item331-cross+line+elbow|wire=packet53-wire55:0-nsew+55:0-ew+55:0-se|oracle=connection-shape-set+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M401_SET=" + evidence);
      System.out.println("WORLDLINE_M401_TRACE=" + trace);
      System.out.println("WORLDLINE_M401_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition dust(B173WireClient a, BlockPosition support) throws Exception {
    BlockPosition target = BlockFace.UP.adjacent(support);
    a.useHeldItemOnBlock(support, BlockFace.UP);
    a.awaitBlock(target, new BlockState(55, 0));
    return target;
  }
  private static String links(RemoteWorldView w, BlockPosition p) {
    String s = "";
    if (w.blockAt(p.x(), p.y(), p.z() - 1).legacyId() == 55)
      s += "n";
    if (w.blockAt(p.x(), p.y(), p.z() + 1).legacyId() == 55)
      s += "s";
    if (w.blockAt(p.x() + 1, p.y(), p.z()).legacyId() == 55)
      s += "e";
    if (w.blockAt(p.x() - 1, p.y(), p.z()).legacyId() == 55)
      s += "w";
    return s;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic remaining-redstone-wire foundation");
  }
  private static String cell(BlockPosition p, int id, int meta) {
    return p.x() + ":" + p.y() + ":" + p.z() + ":" + id + ":" + meta;
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
