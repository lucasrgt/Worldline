package worldline.smoke.boatcurrentpushb173;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places one boat on flowing water 8 and freezes Packet31/33/34 downstream displacement. */
public final class BoatCurrentPushSmoke {
  private BoatCurrentPushSmoke() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 8)
      throw new IllegalArgumentException(
          "usage: BoatCurrentPushSmoke server.jar workspace port seed actor observer chunkX chunkZ");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String actorName = args[4], observerName = args[5];
    int cx = Integer.parseInt(args[6]), cz = Integer.parseInt(args[7]);
    BoatCurrentPushChannel.require(seed == 17320110707L && actorName.equals("BoatCurr579") && actorName.length() <= 16
            && observerName.equals("BoatSee579") && observerName.length() <= 16,
        "boat-current-push identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, actorName, timeout);
    B173WireClient observer = new B173WireClient("127.0.0.1", port, observerName, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {1, 3, 9, 333}, new int[] {64, 1, 1, 1}, new int[] {0, 0, 0, 0});
      B173PlayerSeed.write(workspace, observerName, 4.5D, 80D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      BoatCurrentPushChannel.require(actor.awaitInventory().occupiedSlots() == 4, "boat-current inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BoatCurrentPushChannel channel = BoatCurrentPushChannel.build(actor, initial, cx, cz);
      observer.connect();
      observer.synchronizePose();
      observer.awaitRemoteChunk(cx, cz);
      BoatCurrentPushChannel.station(actor, channel.flow.x() + 0.5D, channel.flow.y() + 0.1D, channel.flow.z() + 0.5D);
      actor.selectHeldSlot(3);
      actor.look(0F, 90F);
      actor.useSelectedItemInAir();
      RemoteObjectSpawn spawn = B173BoatCurrent.awaitSpawn(actor);
      RemoteObjectSpawn peer = B173BoatCurrent.awaitSpawn(observer);
      BoatCurrentPushChannel.require(spawn.equals(peer) && spawn.entityId() != actor.state().entityId()
              && spawn.entityId() != observer.state().entityId(),
          "peer boat spawn drift");
      BoatCurrentPushChannel.require(Math.abs(spawn.x() - (channel.flow.x() + 0.5D)) <= 4D
              && Math.abs(spawn.z() - (channel.flow.z() + 0.5D)) <= 4D,
          "boat packet pose escaped flowing cell pose=" + spawn.x() + ":" + spawn.y() + ":" + spawn.z());
      BoatCurrentPushChannel.station(
          actor, channel.south.x() + 0.5D, channel.south.y() + 1.1D, channel.south.z() + 0.5D);
      RemoteObjectMovement move = B173BoatCurrent.awaitDownstream(actor, spawn, 1, 0);
      RemoteObjectMovement peerMove = B173BoatCurrent.awaitDownstream(observer, spawn, 1, 0);
      BoatCurrentPushChannel.require(move.entityId() == spawn.entityId() && peerMove.entityId() == spawn.entityId()
              && (move.packetId() == 31 || move.packetId() == 33 || move.packetId() == 34)
              && (peerMove.packetId() == 31 || peerMove.packetId() == 33 || peerMove.packetId() == 34)
              && move.toFixedX() - spawn.fixedX() > 0 && peerMove.toFixedX() - spawn.fixedX() > 0,
          "peer boat current displacement drift");
      actor.close();
      observer.close();
      BoatCurrentPushChannel.awaitPlayers(server, 0);
      server.save();
      String evidence = "column=" + channel.column + ",source=" + BoatCurrentPushChannel.cell(channel.source)
          + ":9:0,flow=" + BoatCurrentPushChannel.cell(channel.flow) + ":" + channel.fluid + ":" + channel.meta
          + ",boat=type" + spawn.type() + "+shared-id+packet23+packet31|33|34-downstream,spawn=" + spawn.fixedX()
          + ":" + spawn.fixedY() + ":" + spawn.fixedZ() + ",clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed + "|fixture=raised-stone-channel+flowing-water"
          + channel.fluid + "|cause=packet15-dir255-boat333|wire=packet23-type" + spawn.type()
          + "+packet31-or33-or34-downstream|oracle="
          + "two-peer-boat-current-push-not-spawn-only-not-ride-not-break|" + evidence;
      System.out.println("WORLDLINE_M579_SET=" + evidence);
      System.out.println("WORLDLINE_M579_TRACE=" + trace);
      System.out.println("WORLDLINE_M579_SIGNATURE=" + BoatCurrentPushChannel.sha(trace));
    } finally {
      actor.close();
      observer.close();
      server.close();
    }
  }
}
