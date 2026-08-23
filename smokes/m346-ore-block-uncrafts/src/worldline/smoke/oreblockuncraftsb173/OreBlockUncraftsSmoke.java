package worldline.smoke.oreblockuncraftsb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.RemoteInventoryView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173OreBlockUncraftsClick;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;

/** Uncrafts gold 41, iron 42, diamond 57, and lapis 22 to nine items in personal window 0. */
public final class OreBlockUncraftsSmoke {
  private static final String SIGNAL = "result=266x9:0+265x9:0+264x9:0+351x9:4,taken=true,"
      + "stored=36:266x9:0+37:265x9:0+38:264x9:0+39:351x9:4,actions=16,persisted=true,clients=2,disconnect=clean";
  private OreBlockUncraftsSmoke() {
  }

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 5)
      throw new IllegalArgumentException(
          "usage: OreBlockUncraftsSmoke server.jar workspace port seed username");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String user = arguments[4];
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    require(B173OreBlockUncraftsClick.GOLD_BLOCK.legacyId() == 41
            && B173OreBlockUncraftsClick.IRON_BLOCK.legacyId() == 42
            && B173OreBlockUncraftsClick.DIAMOND_BLOCK.legacyId() == 57
            && B173OreBlockUncraftsClick.LAPIS_BLOCK.legacyId() == 22
            && B173OreBlockUncraftsClick.GOLD_INGOTS.legacyId() == 266
            && B173OreBlockUncraftsClick.IRON_INGOTS.legacyId() == 265
            && B173OreBlockUncraftsClick.DIAMONDS.legacyId() == 264
            && B173OreBlockUncraftsClick.LAPIS.legacyId() == 351
            && B173OreBlockUncraftsClick.LAPIS.damage() == 4,
        "ore-block uncraft identities drifted");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = client(port, user, timeout), reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 72D, 4.5D, new int[] {0, 1, 2, 3},
          new int[] {41, 42, 57, 22}, new int[] {1, 1, 1, 1}, new int[] {0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      RemoteInventoryView initial = actor.awaitInventory();
      require(initial.occupiedSlots() == 4
              && initial.slot(36).item().equals(B173OreBlockUncraftsClick.GOLD_BLOCK)
              && initial.slot(37).item().equals(B173OreBlockUncraftsClick.IRON_BLOCK)
              && initial.slot(38).item().equals(B173OreBlockUncraftsClick.DIAMOND_BLOCK)
              && initial.slot(39).item().equals(B173OreBlockUncraftsClick.LAPIS_BLOCK)
              && B173OreBlockUncraftsClick.emptyCraft(initial),
          "ore-block seed drifted");
      B173OreBlockUncraftsClick.gold(actor);
      require(actor.inventory().slot(36).item().equals(B173OreBlockUncraftsClick.GOLD_INGOTS)
              && actor.inventory().slot(36).item().legacyId() == 266
              && B173OreBlockUncraftsClick.emptyCraft(actor.inventory()),
          "uncrafted gold 266 drifted");
      B173OreBlockUncraftsClick.iron(actor);
      require(actor.inventory().slot(37).item().equals(B173OreBlockUncraftsClick.IRON_INGOTS)
              && actor.inventory().slot(37).item().legacyId() == 265
              && B173OreBlockUncraftsClick.emptyCraft(actor.inventory()),
          "uncrafted iron 265 drifted");
      B173OreBlockUncraftsClick.diamond(actor);
      require(actor.inventory().slot(38).item().equals(B173OreBlockUncraftsClick.DIAMONDS)
              && actor.inventory().slot(38).item().legacyId() == 264
              && B173OreBlockUncraftsClick.emptyCraft(actor.inventory()),
          "uncrafted diamond 264 drifted");
      B173OreBlockUncraftsClick.lapis(actor);
      requireStored(actor.inventory());
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      require(
          server.player(user).inventoryItems() == 4, "ore-block uncraft persistence count drifted");
      reader = client(port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      requireStored(reader.awaitInventory());
      reader.close();
      awaitPlayers(server, 0);
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
    String trace = "v1|server=official-b1.7.3|seed=" + seed
        + "|fixture=personal-2x2-gold41+iron42+diamond57+lapis22"
        + "|window0=gold-to-ingot266+iron-to-ingot265+diamond-to-gem264+lapis-to-dye351:4"
        + "|cause=packet102-window0-left|wire=packet106-accepted"
        + "|oracle=result266x9+result265x9+result264x9+result351x9:4+fresh-login|" + SIGNAL;
    System.out.println("WORLDLINE_M346_UNCRAFTS=" + SIGNAL);
    System.out.println("WORLDLINE_M346_TRACE=" + trace);
    System.out.println("WORLDLINE_M346_SIGNATURE=" + sha256(trace));
  }

  private static B173WireClient client(int port, String name, Duration timeout) {
    return new B173WireClient("127.0.0.1", port, name, timeout);
  }
  private static void requireStored(RemoteInventoryView view) {
    require(B173OreBlockUncraftsClick.stored(view) && view.slot(36).item().legacyId() == 266
            && view.slot(37).item().legacyId() == 265 && view.slot(38).item().legacyId() == 264
            && view.slot(39).item().legacyId() == 351 && view.slot(39).item().damage() == 4,
        "stored ore-block uncrafts 266+265+264+351 drifted");
  }
  private static void awaitPlayers(B173DedicatedServer server, int count)
      throws InterruptedException {
    long deadline = System.currentTimeMillis() + 5000L;
    while (System.currentTimeMillis() < deadline) {
      if (server.players().size() == count)
        return;
      Thread.sleep(100L);
    }
    throw new IllegalStateException("player count did not become " + count);
  }
  private static String sha256(String value) throws Exception {
    byte[] bytes =
        MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
    StringBuilder result = new StringBuilder();
    for (byte item : bytes)
      result.append(String.format("%02x", item & 255));
    return result.toString();
  }
  private static void require(boolean condition, String message) {
    if (!condition)
      throw new IllegalStateException(message);
  }
}
