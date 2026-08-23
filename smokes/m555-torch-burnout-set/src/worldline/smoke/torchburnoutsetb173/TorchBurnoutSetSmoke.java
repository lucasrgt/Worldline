package worldline.smoke.torchburnoutsetb173;

import java.nio.file.*;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Rapid Packet15 lever toggles burn cloned M312 wall torch 76:4 out to 75:4, then recover. */
public final class TorchBurnoutSetSmoke {
  private TorchBurnoutSetSmoke() {
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 7)
      throw new IllegalArgumentException(
          "usage: TorchBurnoutSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int cx = Integer.parseInt(args[5]), cz = Integer.parseInt(args[6]);
    Duration timeout = Duration.ofSeconds(90);
    TorchBurnoutSetSupport.require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D,
          new int[] {0, 1, 2, 3, 4, 5, 6}, new int[] {1, 356, 69, 76, 35, 359, 12},
          new int[] {32, 1, 1, 1, 16, 1, 16}, new int[] {0, 0, 0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      TorchBurnoutSetSupport.require(
          actor.awaitInventory().occupiedSlots() == 7, "torch burnout inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      int[] column = new int[1];
      BlockPosition top = TorchBurnoutSetSupport.raise(actor, initial, cx, cz, column);
      actor.moveAndObserve(0D, 0D, 2D, 1);
      BlockPosition east = TorchBurnoutSetSupport.place(actor, top, BlockFace.EAST, 1);
      BlockPosition west = TorchBurnoutSetSupport.place(actor, top, BlockFace.WEST, 1);
      BlockPosition body = TorchBurnoutSetSupport.place(actor, west, BlockFace.UP, 1);
      BlockPosition repeater = BlockFace.UP.adjacent(top);
      BlockPosition lever = BlockFace.UP.adjacent(east);
      BlockPosition torch = BlockFace.NORTH.adjacent(body);
      BlockPosition cell = BlockFace.WEST.adjacent(torch);
      BlockState on = new BlockState(76, 4), off = new BlockState(75, 4),
                 floor = new BlockState(76, 5);
      actor.selectHeldSlot(3);
      actor.placeHeldBlock(body, BlockFace.NORTH);
      TorchBurnoutSetSupport.require(
          actor.awaitBlock(torch, on).blockAt(torch.x(), torch.y(), torch.z()).equals(on)
              && !on.equals(floor),
          "live north torch 76:4 drift");
      actor.look(90F, 0F);
      worldline.test.WorldlineSmokeAwait.observe(actor, 2);
      actor.selectHeldSlot(1);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      TorchBurnoutSetSupport.require(actor.awaitBlock(repeater, new BlockState(93, 3))
                                         .blockAt(repeater.x(), repeater.y(), repeater.z())
                                         .equals(new BlockState(93, 3)),
          "west repeater drift");
      actor.selectHeldSlot(2);
      actor.placeHeldBlock(east, BlockFace.UP);
      BlockState leverOff = worldline.test.WorldlineSmokeAwait.observe(actor, 5).blockAt(
          lever.x(), lever.y(), lever.z());
      TorchBurnoutSetSupport.require(
          leverOff.legacyId() == 69 && (leverOff.metadata() & 8) == 0, "input lever drift");
      BlockState leverOn = new BlockState(69, leverOff.metadata() | 8);
      actor.selectHeldSlot(7);
      worldline.test.WorldlineSmokeAwait.observe(actor, 5);
      actor.activateBlock(lever, BlockFace.UP);
      actor.awaitBlock(lever, leverOn);
      TorchBurnoutSetSupport.require(
          actor.awaitBlock(torch, off).blockAt(torch.x(), torch.y(), torch.z()).equals(off),
          "first invert 75:4 absent");
      actor.activateBlock(lever, BlockFace.UP);
      actor.awaitBlock(lever, leverOff);
      TorchBurnoutSetSupport.require((worldline.test.WorldlineSmokeAwait.observe(actor, 10)
                                             .blockAt(lever.x(), lever.y(), lever.z())
                                             .metadata()
                                         & 8)
              == 0,
          "family lever off drift");
      RemoteWorldView family = TorchBurnoutSetSupport.pokePlace(actor, torch, cell);
      TorchBurnoutSetSupport.require(family.blockAt(torch.x(), torch.y(), torch.z()).equals(on),
          "family return 76:4 absent: " + family.blockAt(torch.x(), torch.y(), torch.z()));
      for (int n = 0; n < 24; n++) {
        actor.activateBlock(lever, BlockFace.UP);
        worldline.test.WorldlineSmokeAwait.observe(actor, 3);
      }
      TorchBurnoutSetSupport.require((worldline.test.WorldlineSmokeAwait.observe(actor, 4)
                                             .blockAt(lever.x(), lever.y(), lever.z())
                                             .metadata()
                                         & 8)
              == 0,
          "spam lever did not end unpowered");
      actor.selectHeldSlot(4);
      BlockPosition cap = BlockFace.UP.adjacent(torch);
      actor.placeHeldBlock(torch, BlockFace.UP);
      actor.awaitBlock(cap, TorchBurnoutSetSupport.WOOL);
      actor.selectHeldSlot(7);
      RemoteWorldView burntView = worldline.test.WorldlineSmokeAwait.observe(actor, 6);
      BlockState burnt = burntView.blockAt(torch.x(), torch.y(), torch.z());
      TorchBurnoutSetSupport.require(
          (burntView.blockAt(lever.x(), lever.y(), lever.z()).metadata() & 8) == 0
              && burnt.equals(off) && !burnt.equals(on) && !burnt.equals(floor),
          "burnout 75:4 after wool-up poke absent: " + burnt);
      worldline.test.WorldlineSmokeAwait.observe(actor, 400);
      TorchBurnoutSetSupport.pokeBreak(actor, cell);
      RemoteWorldView recoveredView = TorchBurnoutSetSupport.pokePlace(actor, torch, cell);
      if (!recoveredView.blockAt(torch.x(), torch.y(), torch.z()).equals(on))
        recoveredView = actor.awaitBlock(torch, on);
      BlockState recovered = recoveredView.blockAt(torch.x(), torch.y(), torch.z());
      TorchBurnoutSetSupport.require(recovered.equals(on) && !recovered.equals(off)
              && (recoveredView.blockAt(lever.x(), lever.y(), lever.z()).metadata() & 8) == 0,
          "recovery 76:4 absent: " + recovered);
      actor.close();
      TorchBurnoutSetSupport.awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockState placed = after.blockAt(TorchBurnoutSetSupport.local(torch.x(), cx), torch.y(),
          TorchBurnoutSetSupport.local(torch.z(), cz));
      TorchBurnoutSetSupport.require(
          after.blockAt(TorchBurnoutSetSupport.local(top.x(), cx), top.y(),
                   TorchBurnoutSetSupport.local(top.z(), cz))
                  .equals(new BlockState(1, 0))
              && placed.equals(on) && placed.legacyId() == 76 && placed.metadata() == 4
              && !placed.equals(off) && !placed.equals(floor),
          "persisted recovered redstone torch drift: " + placed);
      String evidence = "column=" + column[0] + ",support=" + top.x() + ":" + top.y() + ":"
          + top.z() + ":1:0,torch=" + torch.x() + ":" + torch.y() + ":" + torch.z()
          + ":76:4->75:4->76:4,burnout=75:4,recovered=76:4,rapidActivations=24,recoveryWait=400,persisted=76:4,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+inverter+torch76-burnout|cause=packet15-item76-then-rapid-lever-toggles"
          + "|wire=packet53-torch76:4<->torch75:4|oracle=live-on+burnout-off-unpowered+recover-on+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M555_BURNOUT=" + evidence);
      System.out.println("WORLDLINE_M555_TRACE=" + trace);
      System.out.println("WORLDLINE_M555_SIGNATURE=" + TorchBurnoutSetSupport.sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
}
