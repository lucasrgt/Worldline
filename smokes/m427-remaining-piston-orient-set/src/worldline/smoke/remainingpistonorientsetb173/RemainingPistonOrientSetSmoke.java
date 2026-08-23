package worldline.smoke.remainingpistonorientsetb173;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places remaining piston 33 and sticky 29 facings 0,2,3,4,5 as one SET. */
public final class RemainingPistonOrientSetSmoke {
  private RemainingPistonOrientSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RemainingPistonOrientSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    RemainingPistonOrientSetArm.require(
        seed == 17320110707L && user.equals("PistOrient427") && user.length() <= 16,
        "remaining-piston-orient-set identity drift");
    Duration timeout = Duration.ofSeconds(120);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, down33, down29, n33, s33, w33, e33, n29, s29, w29, e29;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2},
          new int[] {1, 33, 29}, new int[] {64, 8, 8}, new int[] {0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      RemainingPistonOrientSetArm.require(
          actor.awaitInventory().occupiedSlots() == 3, "remaining-piston-orient inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = RemainingPistonOrientSetArm.raise(actor, initial, cx, cz, column);
      actor.selectHeldSlot(0);
      BlockPosition east = RemainingPistonOrientSetArm.place(actor, top, BlockFace.EAST, 1),
                    west = RemainingPistonOrientSetArm.place(actor, top, BlockFace.WEST, 1);
      BlockPosition ee = RemainingPistonOrientSetArm.place(actor, east, BlockFace.EAST, 1),
                    eee = RemainingPistonOrientSetArm.place(actor, ee, BlockFace.EAST, 1);
      BlockPosition n33p = RemainingPistonOrientSetArm.place(actor, ee, BlockFace.NORTH, 1),
                    s33p = RemainingPistonOrientSetArm.place(actor, ee, BlockFace.SOUTH, 1);
      BlockPosition w33p = RemainingPistonOrientSetArm.place(actor, eee, BlockFace.NORTH, 1),
                    e33p = RemainingPistonOrientSetArm.place(actor, eee, BlockFace.SOUTH, 1);
      BlockPosition n29p = RemainingPistonOrientSetArm.place(actor, n33p, BlockFace.NORTH, 1),
                    s29p = RemainingPistonOrientSetArm.place(actor, s33p, BlockFace.SOUTH, 1);
      BlockPosition w29p = RemainingPistonOrientSetArm.place(actor, w33p, BlockFace.NORTH, 1),
                    e29p = RemainingPistonOrientSetArm.place(actor, e33p, BlockFace.SOUTH, 1);
      BlockPosition high = RemainingPistonOrientSetArm.stack(actor, east, 8),
                    high2 = RemainingPistonOrientSetArm.stack(actor, west, 8);
      down33 = RemainingPistonOrientSetArm.piston(actor, high, 1, 33, 0, 180F);
      down29 = RemainingPistonOrientSetArm.piston(actor, high2, 2, 29, 0, 180F);
      PlayerPose here = actor.moveBy(0D, 0D, 0D);
      actor.moveAndObserve(
          (top.x() + 0.5D) - here.x(), (top.y() + 1D) - here.y(), (top.z() + 0.5D) - here.z(), 2);
      n33 = RemainingPistonOrientSetArm.piston(actor, n33p, 1, 33, 2, 0F);
      s33 = RemainingPistonOrientSetArm.piston(actor, s33p, 1, 33, 3, 180F);
      w33 = RemainingPistonOrientSetArm.piston(actor, w33p, 1, 33, 4, -90F);
      e33 = RemainingPistonOrientSetArm.piston(actor, e33p, 1, 33, 5, 90F);
      n29 = RemainingPistonOrientSetArm.piston(actor, n29p, 2, 29, 2, 0F);
      s29 = RemainingPistonOrientSetArm.piston(actor, s29p, 2, 29, 3, 180F);
      w29 = RemainingPistonOrientSetArm.piston(actor, w29p, 2, 29, 4, -90F);
      e29 = RemainingPistonOrientSetArm.piston(actor, e29p, 2, 29, 5, 90F);
      actor.close();
      RemainingPistonOrientSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      RemainingPistonOrientSetArm.persist(after, cx, cz, down33, 33, 0, "down piston 33:0");
      RemainingPistonOrientSetArm.persist(after, cx, cz, n33, 33, 2, "north piston 33:2");
      RemainingPistonOrientSetArm.persist(after, cx, cz, s33, 33, 3, "south piston 33:3");
      RemainingPistonOrientSetArm.persist(after, cx, cz, w33, 33, 4, "west piston 33:4");
      RemainingPistonOrientSetArm.persist(after, cx, cz, e33, 33, 5, "east piston 33:5");
      RemainingPistonOrientSetArm.persist(after, cx, cz, down29, 29, 0, "down sticky 29:0");
      RemainingPistonOrientSetArm.persist(after, cx, cz, n29, 29, 2, "north sticky 29:2");
      RemainingPistonOrientSetArm.persist(after, cx, cz, s29, 29, 3, "south sticky 29:3");
      RemainingPistonOrientSetArm.persist(after, cx, cz, w29, 29, 4, "west sticky 29:4");
      RemainingPistonOrientSetArm.persist(after, cx, cz, e29, 29, 5, "east sticky 29:5");
      String evidence = "column=" + column[0]
          + ",support=" + RemainingPistonOrientSetArm.token(top, 1, 0)
          + ",piston=" + RemainingPistonOrientSetArm.token(down33, 33, 0) + "+"
          + RemainingPistonOrientSetArm.token(n33, 33, 2) + "+"
          + RemainingPistonOrientSetArm.token(s33, 33, 3) + "+"
          + RemainingPistonOrientSetArm.token(w33, 33, 4) + "+"
          + RemainingPistonOrientSetArm.token(e33, 33, 5)
          + ",sticky=" + RemainingPistonOrientSetArm.token(down29, 29, 0) + "+"
          + RemainingPistonOrientSetArm.token(n29, 29, 2) + "+"
          + RemainingPistonOrientSetArm.token(s29, 29, 3) + "+"
          + RemainingPistonOrientSetArm.token(w29, 29, 4) + "+"
          + RemainingPistonOrientSetArm.token(e29, 29, 5)
          + ",look=down-stack+0+90+180+-90,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+piston33-remaining+stickypiston29-remaining|cause=packet15-item33+item29+look-down-stack+look0+look90+look180+look-90|wire=packet53-piston33:0+33:2+33:3+33:4+33:5+stickypiston29:0+29:2+29:3+29:4+29:5|oracle=remaining-place-facings+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M427_SET=" + evidence);
      System.out.println("WORLDLINE_M427_TRACE=" + trace);
      System.out.println("WORLDLINE_M427_SIGNATURE=" + RemainingPistonOrientSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
