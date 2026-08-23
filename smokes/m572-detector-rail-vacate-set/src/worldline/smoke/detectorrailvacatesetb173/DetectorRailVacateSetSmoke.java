package worldline.smoke.detectorrailvacatesetb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteObjectSpawn;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Occupied detector 28 emits power; after the minecart leaves the rail unpowers. */
public final class DetectorRailVacateSetSmoke {
  private DetectorRailVacateSetSmoke() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 7)
      throw new IllegalArgumentException(
          "usage: DetectorRailVacateSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
    DetectorRailVacateSetArm.require(seed == 17320110707L && user.equals("DetVacate572") && user.length() <= 16,
        "detector-rail-vacate identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    DetectorRailVacateSetArm arm;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 28, 66, 328}, new int[] {32, 1, 4, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      DetectorRailVacateSetArm.require(
          actor.awaitInventory().occupiedSlots() == 4, "detector-rail-vacate inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      arm = DetectorRailVacateSetArm.place(actor, initial, cx, cz, column);
      BlockState idle = actor.awaitRemoteChunk(cx, cz).blockAt(arm.detector.x(), arm.detector.y(), arm.detector.z());
      actor.selectHeldSlot(3);
      actor.useHeldItemOnBlock(arm.detector, BlockFace.UP);
      RemoteObjectSpawn cart = actor.awaitObjectSpawn(10);
      DetectorRailVacateSetArm.require(cart.type() == 10 && cart.throwerId() == 0 && cart.velocityX() == 0
              && cart.velocityY() == 0 && cart.velocityZ() == 0 && cart.fixedX() == arm.detector.x() * 32 + 16
              && cart.fixedZ() == arm.detector.z() * 32 + 16,
          "minecart packet23 type10 spawn bounds drift");
      BlockState occupied = arm.occupy(actor);
      DetectorRailVacateSetArm.require(
          occupied.legacyId() == 28 && (occupied.metadata() & 8) != 0 && !occupied.equals(idle),
          "occupied detector power absent");
      BlockState vacated = arm.vacate(actor);
      DetectorRailVacateSetArm.require(vacated.legacyId() == 28 && (vacated.metadata() & 8) == 0 && vacated.equals(idle)
              && !vacated.equals(occupied),
          "vacated detector unpower absent");
      actor.close();
      DetectorRailVacateSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      reader.awaitBlock(arm.detector, vacated);
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      arm.persist(after, cx, cz, vacated);
      String evidence = "column=" + column[0] + ",support=" + DetectorRailVacateSetArm.token(arm.support, 1, 0)
          + ",high=" + DetectorRailVacateSetArm.token(arm.high, 1, 0)
          + ",detector=" + DetectorRailVacateSetArm.cell(arm.detector) + ":28:" + idle.metadata() + "->"
          + occupied.metadata() + "->" + vacated.metadata() + ",landing="
          + DetectorRailVacateSetArm.token(arm.landing, 66,
              after.blockAt(arm.landing.x() - cx * 16, arm.landing.y(), arm.landing.z() - cz * 16).metadata())
          + ",cart=type10+thrower0+fixed" + cart.fixedX() + ":" + cart.fixedY() + ":" + cart.fixedZ() + ",occupy=28:"
          + occupied.metadata() + ",vacate=28:" + vacated.metadata() + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+sloped-detector28+landing-rail66+minecart328"
          + "|cause=packet15-item28+packet15-item66+packet15-minecart328"
          + "|wire=packet23-type10+thrower0+packet53-detector28-occupy-then-vacate"
          + "|oracle=occupy-then-vacate-detector+fresh-login|" + evidence;
      System.out.println("WORLDLINE_M572_SET=" + evidence);
      System.out.println("WORLDLINE_M572_TRACE=" + trace);
      System.out.println("WORLDLINE_M572_SIGNATURE=" + DetectorRailVacateSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
