package worldline.smoke.pistonimmovablesetb173;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Official piston 33 fails to push chest 54, furnace 61, and mob spawner 52 in one cycle. */
public final class PistonImmovableSetSmoke {
  private PistonImmovableSetSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 9)
      throw new IllegalArgumentException(
          "usage: PistonImmovableSetSmoke server.jar workspace port seed username chunkX chunkZ fixtureTicks signalTicks");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]), fixture = Integer.parseInt(a[7]),
        signal = Integer.parseInt(a[8]);
    Duration timeout = Duration.ofSeconds(90);
    PistonImmovableSetArm.require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    PistonImmovableSetArm chest, furnace, spawner;
    int[] column = new int[1];
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4, 5},
          new int[] {1, 33, 54, 61, 52, 69}, new int[] {32, 3, 1, 1, 1, 3},
          new int[] {0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      actor.look(-90F, 0F);
      PistonImmovableSetArm.require(
          actor.awaitInventory().occupiedSlots() == 6, "piston-immovable inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = PistonImmovableSetArm.raise(actor, initial, cx, cz, column);
      chest = PistonImmovableSetArm.place(actor, initial, top, cx, cz, 1, 2, 54, 5);
      actor.selectHeldSlot(0);
      actor.moveAndObserve(2D, 0D, 0D, 2);
      BlockPosition furnaceSupport = PistonImmovableSetArm.padSouth(actor, top);
      furnace = PistonImmovableSetArm.place(actor, initial, furnaceSupport, cx, cz, 1, 3, 61, 5);
      BlockPosition spawnerSupport = PistonImmovableSetArm.padSouth(actor, furnace.support);
      spawner = PistonImmovableSetArm.place(actor, initial, spawnerSupport, cx, cz, 1, 4, 52, 5);
      actor.selectHeldSlot(6);
      RemoteWorldView settled = worldline.test.WorldlineSmokeAwait.observe(actor, fixture);
      PistonImmovableSetArm.require(
          settled.blockAt(chest.piston.x(), chest.piston.y(), chest.piston.z())
                  .equals(new BlockState(33, 4))
              && settled.blockAt(chest.payload.x(), chest.payload.y(), chest.payload.z())
                  .equals(chest.payloadState)
              && settled
                  .blockAt(chest.destination.x(), chest.destination.y(), chest.destination.z())
                  .equals(new BlockState(0, 0)),
          "chest 54 precondition drift");
      PistonImmovableSetArm.require(
          settled.blockAt(furnace.piston.x(), furnace.piston.y(), furnace.piston.z())
                  .equals(new BlockState(33, 4))
              && settled.blockAt(furnace.payload.x(), furnace.payload.y(), furnace.payload.z())
                  .equals(furnace.payloadState)
              && settled
                  .blockAt(
                      furnace.destination.x(), furnace.destination.y(), furnace.destination.z())
                  .equals(new BlockState(0, 0)),
          "furnace 61 precondition drift");
      PistonImmovableSetArm.require(
          settled.blockAt(spawner.piston.x(), spawner.piston.y(), spawner.piston.z())
                  .equals(new BlockState(33, 4))
              && settled.blockAt(spawner.payload.x(), spawner.payload.y(), spawner.payload.z())
                  .equals(spawner.payloadState)
              && settled
                  .blockAt(
                      spawner.destination.x(), spawner.destination.y(), spawner.destination.z())
                  .equals(new BlockState(0, 0)),
          "spawner 52 precondition drift");
      actor.moveAndObserve(0D, 0D, -4D, 4);
      chest.pulse(actor, signal);
      actor.moveAndObserve(0D, 0D, 2D, 2);
      furnace.pulse(actor, signal);
      actor.moveAndObserve(0D, 0D, 2D, 2);
      spawner.pulse(actor, signal);
      actor.close();
      PistonImmovableSetArm.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      chest.persist(after, cx, cz);
      furnace.persist(after, cx, cz);
      spawner.persist(after, cx, cz);
      String evidence = "column=" + column[0] + ",chest=" + chest.token() + ",furnace="
          + furnace.token() + ",spawner=" + spawner.token() + ",piston33=4->4,chest-arm="
          + chest.arm() + ",furnace-arm=" + furnace.arm() + ",spawner-arm=" + spawner.arm()
          + ",retracted=true,persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=piston33-west+chest54+furnace61+spawner52|settle=" + fixture + "+" + signal
          + "ticks|cause=packet15-lever-activate|effect=official-piston33-immovable-set|observation=fresh-login-packet51|"
          + evidence;
      System.out.println("WORLDLINE_M553_SET=" + evidence);
      System.out.println("WORLDLINE_M553_TRACE=" + trace);
      System.out.println("WORLDLINE_M553_SIGNATURE=" + PistonImmovableSetArm.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
