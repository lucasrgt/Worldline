package worldline.smoke.doublechestmergesetb173;
import static worldline.b173server.B173FixtureSupport.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteContainerWindow;
import worldline.api.RemoteWindowClosure;
import worldline.api.RemoteWindowDescriptor;
import worldline.api.RemoteWindowKind;
import worldline.api.RemoteWorldView;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Adjacent chest 54 placement merges a 27-slot Chest into a 54-slot Large chest. */
public final class DoubleChestMergeSetSmoke {
  private DoubleChestMergeSetSmoke() {}

  public static void main(String[] arguments) throws Exception {
    if (arguments.length != 7)
      throw new IllegalArgumentException(
          "usage: DoubleChestMergeSetSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(arguments[0]), workspace = Paths.get(arguments[1]);
    int port = Integer.parseInt(arguments[2]);
    long seed = Long.parseLong(arguments[3]);
    String user = arguments[4];
    int cx = Integer.parseInt(arguments[5]), cz = Integer.parseInt(arguments[6]);
    require(seed == 17320110707L && user.equals("DblChest611") && user.length() <= 16 && cx == 0 && cz == 0,
        "double-chest-merge identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    B173WireClient reader = null;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(
          workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1}, new int[] {1, 54}, new int[] {32, 2}, new int[] {0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 2, "double-chest-merge inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      BlockPosition top = foundation(initial, cx, cz);
      int column = 0;
      actor.selectHeldSlot(0);
      while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded double-chest-merge fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      BlockPosition east = place(actor, top, BlockFace.EAST, 1);
      actor.moveAndObserve(0D, 0D, 1D, 2);
      actor.selectHeldSlot(1);
      BlockPosition left = place(actor, top, BlockFace.UP, 54);
      BlockState chest = new BlockState(54, 0);
      RemoteWorldView first = WorldlineSmokeAwait.observe(actor, 5);
      require(first.blockAt(left.x(), left.y(), left.z()).equals(chest), "live first chest 54:0 drift");
      actor.selectHeldSlot(2);
      RemoteContainerWindow single = actor.openChest(left, BlockFace.UP);
      require(window(single, "Chest", 27, 63), "live Packet100 single-chest window absent");
      RemoteWindowClosure closed = actor.closeWindow();
      require(window(closed.closedWindow(), "Chest", 27, 63), "single-chest close drift");
      actor.selectHeldSlot(1);
      BlockPosition right = place(actor, east, BlockFace.UP, 54);
      RemoteWorldView live = WorldlineSmokeAwait.observe(actor, 5);
      require(live.blockAt(left.x(), left.y(), left.z()).equals(chest)
              && live.blockAt(right.x(), right.y(), right.z()).equals(chest),
          "live adjacent chest 54:0 drift");
      RemoteContainerWindow merged = actor.openChest(left, BlockFace.UP);
      require(window(merged, "Large chest", 54, 90), "live Packet100 large-chest window absent");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      require(reader.awaitInventory().occupiedSlots() >= 1, "reload inventory drift");
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(top.x(), cx), top.y(), local(top.z(), cz)).equals(new BlockState(1, 0))
              && after.blockAt(local(east.x(), cx), east.y(), local(east.z(), cz)).equals(new BlockState(1, 0))
              && after.blockAt(local(left.x(), cx), left.y(), local(left.z(), cz)).equals(chest)
              && after.blockAt(local(right.x(), cx), right.y(), local(right.z(), cz)).equals(chest),
          "persisted adjacent chest 54:0 drift");
      reader.selectHeldSlot(1);
      RemoteContainerWindow again = reader.openChest(left, BlockFace.UP);
      require(window(again, "Large chest", 54, 90), "fresh-login Packet100 large-chest window absent");
      String evidence = "column=" + column + ",support=" + cell(top, 1, 0) + ",east=" + cell(east, 1, 0)
          + ",left=" + cell(left, 54, 0) + ",right=" + cell(right, 54, 0)
          + ",single=title=Chest,owned=27,total=63,merged=title=Large chest,owned=54,total=90"
          + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed + "|fixture=raised-stone+two-adjacent-chest54"
          + "|cause=packet15-item54+packet100-owned27-title-Chest+packet15-item54"
          + "+packet100-readUTF-owned54-title-Large-chest"
          + "|wire=packet53-chest54:0+packet53-chest54:0+packet100-single27-then-merged54"
          + "|oracle=double-chest-merge-not-single-place-orient|" + evidence;
      System.out.println("WORLDLINE_M611_SET=" + evidence);
      System.out.println("WORLDLINE_M611_TRACE=" + trace);
      System.out.println("WORLDLINE_M611_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }

  private static boolean window(RemoteContainerWindow view, String title, int owned, int total) {
    RemoteWindowDescriptor descriptor = view.descriptor();
    if (descriptor.kind() != RemoteWindowKind.CHEST || !title.equals(descriptor.title())
        || descriptor.containerSlots() != owned || view.inventory().size() != total)
      return false;
    for (int slot = 0; slot < owned; slot++)
      if (!view.inventory().slot(slot).empty())
        return false;
    return true;
  }

  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic double-chest-merge foundation");
  }

  private static String cell(BlockPosition position, int id, int meta) {
    return position.x() + ":" + position.y() + ":" + position.z() + ":" + id + ":" + meta;
  }

  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
