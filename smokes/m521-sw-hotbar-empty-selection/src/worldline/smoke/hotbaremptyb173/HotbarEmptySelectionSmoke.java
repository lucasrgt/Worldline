package worldline.smoke.hotbaremptyb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteHeldItem;
import worldline.api.RemoteInventoryView;
import worldline.api.RemoteItemStack;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173HotbarSelectionAccess;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Proves empty, populated, and rejected hotbar selection through Packet5. */
public final class HotbarEmptySelectionSmoke {
  private HotbarEmptySelectionSmoke() {
  }
  public static void main(String[] args) throws Exception {
    if (args.length != 6)
      throw new IllegalArgumentException("invalid M521 arguments");
    Path jar = Paths.get(args[0]), workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String actorName = args[4], observerName = args[5];
    require(
        seed == 17320110707L && actorName.equals("Hotbar521A") && observerName.equals("Hotbar521B"),
        "M521 identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = client(port, actorName, timeout);
    B173WireClient observer = client(port, observerName, timeout);
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, actorName, 4.5D, 60D, 4.5D, new int[] {0},
          new int[] {1}, new int[] {1}, new int[] {0});
      B173PlayerSeed.write(workspace, observerName, 4.5D, 60D, 4.5D);
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView inventory = actor.awaitInventory();
      require(inventory.occupiedSlots() == 1
              && inventory.slot(36).item().equals(new RemoteItemStack(1, 1, 0))
              && inventory.slot(37).empty(),
          "hotbar seed drift");
      actor.selectHeldSlot(0);
      observer.connect();
      observer.synchronizePose();
      require(observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0)).legacyId() == 1,
          "initial populated Packet5 state absent");
      actor.selectHeldSlot(1);
      worldline.test.WorldlineSmokeAwait.observe(actor, 2);
      require(B173HotbarSelectionAccess.selectedEmpty(actor), "selected slot 1 was not empty");
      require(observer.awaitPeerHeldItem(RemoteHeldItem.empty(actorName)).empty(),
          "explicit empty Packet5 absent");
      actor.selectHeldSlot(0);
      worldline.test.WorldlineSmokeAwait.observe(actor, 2);
      RemoteHeldItem populated = observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0));
      require(populated.legacyId() == 1, "populated Packet5 did not return");
      B173HotbarSelectionAccess.sendInvalidSlot(actor, 9);
      worldline.test.WorldlineSmokeAwait.observe(actor, 10);
      require(!B173HotbarSelectionAccess.selectedEmpty(actor)
              && observer.awaitPeerHeldItem(new RemoteHeldItem(actorName, 1, 0)).legacyId() == 1,
          "invalid slot 9 changed authoritative selection");
    } finally {
      actor.close();
      observer.close();
      server.close();
    }
    String evidence = "slot1=empty,packet5=-1:0,slot0=1:0,slot9=rejected,selection=slot0";
    String trace = "v1|server=official-b1.7.3|fixture=slot0-stone1+slot1-empty"
        + "|cause=packet16-slot1+slot0+invalid9|wire=packet5-empty-minus1-damage0+stone1"
        + "|oracle=actor-inventory+independent-peer-equipment|" + evidence;
    System.out.println("WORLDLINE_M521_SET=" + evidence);
    System.out.println("WORLDLINE_M521_TRACE=" + trace);
    System.out.println("WORLDLINE_M521_SIGNATURE=" + sha(trace));
  }
  private static B173WireClient client(int port, String name, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, name, timeout);
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
