package worldline.smoke.protocol14edgeb173;

import static worldline.b173server.B173FixtureSupport.awaitPlayers;
import static worldline.b173server.B173FixtureSupport.place;
import static worldline.b173server.B173FixtureSupport.sha;
import static worldline.b173server.B173FixtureSupport.water;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteKeepAliveTimeout;
import worldline.api.RemoteProtocol14Chain;
import worldline.api.RemoteSignText;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173Protocol14Access;
import worldline.b173server.B173SignAccess;
import worldline.b173server.B173WireClient;
import worldline.testkit.Protocol14EdgeFixture;

/** Proves Packet130-to-131 framing and the official silent-client timeout. */
public final class Protocol14EdgeSmoke {
  private static final String ACTOR = "EdgeActor631";
  private static final String SILENT = "EdgeSilent631";
  private Protocol14EdgeSmoke() { }

  public static void main(String[] a) throws Exception {
    if (a.length != 4) throw new IllegalArgumentException(
        "usage: Protocol14EdgeSmoke server.jar workspace port seed");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    Duration timeout = Duration.ofSeconds(180);
    B173DedicatedServer server = new B173DedicatedServer(
        jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = client(port, ACTOR, timeout);
    B173WireClient silent = client(port, SILENT, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, ACTOR, 4.5D, 60D, 4.5D,
          new int[] {0, 1, 2}, new int[] {1, 323, 358},
          new int[] {32, 1, 1}, new int[] {0, 0, 0});
      B173PlayerSeed.write(workspace, SILENT, 4.5D, 60D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 3, "edge inventory seed drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(0, 0).chunkAt(0, 0);
      BlockPosition top = foundation(initial);
      actor.selectHeldSlot(0);
      int column = 0;
      while (water(initial.blockAt(top.x(), top.y() + 1, top.z()).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded edge fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      actor.selectHeldSlot(1);
      actor.look(-90F, 0F);
      BlockPosition signCell = BlockFace.UP.adjacent(top);
      actor.useHeldItemOnBlock(top, BlockFace.UP);
      worldline.test.WorldlineSmokeAwait.awaitBlockMatching(
          actor, signCell, value -> value.legacyId() == 63, "standing sign", 40);
      actor.selectHeldSlot(2);
      RemoteSignText expectedSign = new RemoteSignText(
          signCell, "World", "line", "M631", "edge");
      B173Protocol14Access.reset(actor);
      B173SignAccess.update(actor, expectedSign);
      actor.moveAndObserve(1D, 0D, 0D, 20);
      RemoteProtocol14Chain chain = B173Protocol14Access.awaitSignThenMap(actor, expectedSign);
      actor.close();
      awaitPlayers(server, 0);

      silent.connect();
      silent.synchronizePose();
      silent.awaitInventory();
      awaitPlayers(server, 1);
      RemoteKeepAliveTimeout idle = B173Protocol14Access.awaitSilentTimeout(
          silent, Duration.ofSeconds(120));
      String timeoutRecord = server.awaitReadTimeout(SILENT);
      awaitPlayers(server, 0);
      Protocol14EdgeFixture.Evidence evidence = Protocol14EdgeFixture.observe(
          expectedSign, chain, idle, timeoutRecord);
      require(evidence.ordered() && evidence.itemId() == 358 && evidence.mapId() == 0
              && evidence.boundedPayload() && evidence.keepAliveAbsent()
              && evidence.timeoutReason().equals("socket-read-timeout"),
          "protocol-14 fixture evidence drifted");
      String signal = "order=0x82>0x83,sign=packet130,map=packet131:358:0,"
          + "payload=bounded,keepalive=not-emitted,timeout=socket-read-timeout,"
          + "clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+standing-sign+held-map358+silent-client"
          + "|cause=packet130-sign-update+post-sign-move+selected-map-tick"
          + "+silent-play-session"
          + "|wire=packet130+packet131+no-packet0+eof+socket-read-timeout"
          + "+disconnect.genericReason"
          + "|oracle=ordered-edge-packet-chain+keepalive-timeout|" + signal;
      System.out.println("WORLDLINE_M631_PROTOCOL=" + signal);
      System.out.println("WORLDLINE_M631_TRACE=" + trace);
      System.out.println("WORLDLINE_M631_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      silent.close();
      server.close();
    }
  }

  private static B173WireClient client(int port, String username, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, username, timeout);
  }
  private static BlockPosition foundation(RemoteChunkSnapshot chunk) {
    for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++)
      for (int y = 126; y >= 1; y--)
        if (chunk.blockAt(x, y, z).legacyId() == 3
            && water(chunk.blockAt(x, y + 1, z).legacyId()))
          return new BlockPosition(x, y, z);
    throw new IllegalStateException("no deterministic edge-packet foundation");
  }
  private static void require(boolean value, String message) {
    if (!value) throw new IllegalStateException(message);
  }
}
