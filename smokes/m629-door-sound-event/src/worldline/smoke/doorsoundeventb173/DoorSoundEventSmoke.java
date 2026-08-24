package worldline.smoke.doorsoundeventb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;
import worldline.testkit.DoorSoundFixture;

/** Proves that direct wooden-door activation emits Packet61 effect 1003 at the lower half. */
public final class DoorSoundEventSmoke {
  private DoorSoundEventSmoke() { }

  public static void main(String[] a) throws Exception {
    if (a.length != 7) throw new IllegalArgumentException(
        "usage: DoorSoundEventSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(
        jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    B173WireClient observer = new B173WireClient("127.0.0.1", port, "SoundPeer629", timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
          new int[] {0, 1}, new int[] {1, 324}, new int[] {32, 1}, new int[] {0, 0});
      B173PlayerSeed.write(workspace, "SoundPeer629", 4.5D, 60D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "door inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(initial, cx, cz);
      int column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(local(top.x(), cx), top.y() + 1,
              local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded door sound fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      BlockPosition lower = BlockFace.UP.adjacent(top);
      BlockPosition upper = BlockFace.UP.adjacent(lower);
      actor.look(-90F, 0F);
      actor.selectHeldSlot(1);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      RemoteWorldView closed = awaitDoor(actor, lower, upper, 0, 8);
      observer.connect();
      observer.synchronizePose();
      observer.awaitRemoteChunk(cx, cz);
      while (B173WorldEventAccess.poll(observer) != null) { }
      actor.selectHeldSlot(2);
      actor.activateBlock(lower, BlockFace.UP);
      RemoteWorldEvent event = B173WorldEventAccess.await(observer, 1003, lower);
      RemoteWorldView open = awaitDoor(actor, lower, upper, 4, 12);
      DoorSoundFixture.Evidence evidence = DoorSoundFixture.observe(lower,
          closed.blockAt(lower.x(), lower.y(), lower.z()),
          closed.blockAt(upper.x(), upper.y(), upper.z()),
          open.blockAt(lower.x(), lower.y(), lower.z()),
          open.blockAt(upper.x(), upper.y(), upper.z()), event);
      require(evidence.effectId() == 1003 && evidence.lowerHalf(), "fixture evidence drift");
      String signal = "door=64:0/8->4/12,effect=1003:0,event-position=lower,packet=61,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+woodendoor64:0/8+nearby-observer"
          + "|cause=actor-packet15-empty-hand-lower-door-activate"
          + "|wire=observer-packet61-effect1003-data0+actor-packet53-door64:4/12"
          + "|oracle=remote-peer-observes-door-world-event-at-lower-half|" + signal;
      System.out.println("WORLDLINE_M629_SOUND=" + signal);
      System.out.println("WORLDLINE_M629_TRACE=" + trace);
      System.out.println("WORLDLINE_M629_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      observer.close();
      server.close();
    }
  }

  private static RemoteWorldView awaitDoor(B173WireClient actor, BlockPosition lower,
      BlockPosition upper, int lowerMetadata, int upperMetadata) throws Exception {
    RemoteWorldView view = actor.awaitBlock(lower, new BlockState(64, lowerMetadata));
    if (!view.blockAt(upper.x(), upper.y(), upper.z()).equals(new BlockState(64, upperMetadata)))
      view = actor.awaitBlock(upper, new BlockState(64, upperMetadata));
    require(view.blockAt(lower.x(), lower.y(), lower.z()).equals(new BlockState(64, lowerMetadata))
            && view.blockAt(upper.x(), upper.y(), upper.z()).equals(new BlockState(64, upperMetadata)),
        "wooden door state drifted");
    return view;
  }

  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++)
      for (int y = 126; y >= 1; y--)
        if (chunk.blockAt(x, y, z).legacyId() == 3
            && water(chunk.blockAt(x, y + 1, z).legacyId()))
          return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic door sound foundation");
  }
  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
}
