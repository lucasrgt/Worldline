package worldline.smoke.irondoorhandrejectionb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;
import worldline.testkit.IronDoorHandFixture;

/** Proves that empty-hand activation of a placed closed iron door is rejected. */
public final class IronDoorHandRejectionSmoke {
  private static final BlockState CLOSED_LOWER = new BlockState(71, 0);
  private static final BlockState CLOSED_UPPER = new BlockState(71, 8);

  private IronDoorHandRejectionSmoke() { }

  public static void main(String[] a) throws Exception {
    if (a.length != 7) throw new IllegalArgumentException(
        "usage: IronDoorHandRejectionSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = OfficialServerBootstrap.start(
        jar, workspace, port, seed, timeout);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
          new int[] {0, 1}, new int[] {1, 330}, new int[] {32, 1}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "iron door inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(initial, cx, cz);
      int column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(local(top.x(), cx), top.y() + 1,
              local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded iron door hand fixture");
      }
      top = raiseColumn(actor, top, 8);
      column += 8;
      BlockPosition lower = BlockFace.UP.adjacent(top);
      BlockPosition upper = BlockFace.UP.adjacent(lower);
      actor.look(-90F, 0F);
      actor.selectHeldSlot(1);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      RemoteWorldView placed = awaitClosed(actor, lower, upper);
      actor.selectHeldSlot(2);
      actor.activateBlock(lower, BlockFace.UP);
      RemoteWorldView afterLowerHand =
          settledView(worldline.test.WorldlineSmokeAwait.observe(actor, 30), lower, upper);
      actor.activateBlock(upper, BlockFace.UP);
      RemoteWorldView afterUpperHand =
          settledView(worldline.test.WorldlineSmokeAwait.observe(actor, 30), lower, upper);
      IronDoorHandFixture.Evidence evidence = IronDoorHandFixture.observe(lower,
          placed.blockAt(lower.x(), lower.y(), lower.z()),
          placed.blockAt(upper.x(), upper.y(), upper.z()),
          afterLowerHand.blockAt(lower.x(), lower.y(), lower.z()),
          afterLowerHand.blockAt(upper.x(), upper.y(), upper.z()),
          afterUpperHand.blockAt(lower.x(), lower.y(), lower.z()),
          afterUpperHand.blockAt(upper.x(), upper.y(), upper.z()));
      require(evidence.handsRejected() && evidence.preservedHalves(),
          "iron door hand rejection evidence drifted");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      B173WireClient reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot persisted = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      boolean persistedClosed =
          persisted.blockAt(local(lower.x(), cx), lower.y(), local(lower.z(), cz))
              .equals(CLOSED_LOWER)
          && persisted.blockAt(local(upper.x(), cx), upper.y(), local(upper.z(), cz))
              .equals(CLOSED_UPPER);
      reader.close();
      require(persistedClosed, "persisted iron door halves drifted");
      String signal = "door=71:0/8,lower-hand=rejected,upper-hand=rejected,"
          + "preserved=71:0/8,persisted=closed,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+closed-irondoor71:0/8"
          + "|cause=packet15-empty-hand-activate-lower-half+upper-half"
          + "|wire=no-block-change-after-settled-ticks"
          + "|oracle=iron-door-hand-rejection-preserves-both-closed-halves"
          + "-not-m118-redstone-not-m277-wooden-toggle|" + signal;
      System.out.println("WORLDLINE_M751_DOOR=" + signal);
      System.out.println("WORLDLINE_M751_TRACE=" + trace);
      System.out.println("WORLDLINE_M751_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      server.close();
    }
  }

  private static RemoteWorldView awaitClosed(B173WireClient client, BlockPosition lower,
      BlockPosition upper) throws Exception {
    RemoteWorldView view = client.awaitBlock(lower, CLOSED_LOWER);
    if (!view.blockAt(upper.x(), upper.y(), upper.z()).equals(CLOSED_UPPER))
      view = client.awaitBlock(upper, CLOSED_UPPER);
    return settledView(view, lower, upper);
  }

  private static RemoteWorldView settledView(RemoteWorldView view, BlockPosition lower,
      BlockPosition upper) {
    BlockState low = view.blockAt(lower.x(), lower.y(), lower.z());
    BlockState high = view.blockAt(upper.x(), upper.y(), upper.z());
    require(low.equals(CLOSED_LOWER) && high.equals(CLOSED_UPPER),
        "closed iron door halves " + low + " / " + high);
    return view;
  }

  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++)
      for (int y = 126; y >= 1; y--)
        if (chunk.blockAt(x, y, z).legacyId() == 3
            && water(chunk.blockAt(x, y + 1, z).legacyId()))
          return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic iron door hand foundation");
  }
  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
}
