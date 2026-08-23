package worldline.smoke.railpowerb173;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.Duration;
import worldline.api.*;
import worldline.b173server.*;

/** Places detector 28 with minecart 328 as 28:8 and powered rail 27 beside torch 76 as 27:8. */
public final class RailPowerSmoke {
  private RailPowerSmoke() {
  }
  public static void main(String[] a) throws Exception {
    if (a.length != 7)
      throw new IllegalArgumentException(
          "usage: RailPowerSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(a[0]), workspace = Paths.get(a[1]);
    int port = Integer.parseInt(a[2]);
    long seed = Long.parseLong(a[3]);
    String user = a[4];
    int cx = Integer.parseInt(a[5]), cz = Integer.parseInt(a[6]);
    Duration timeout = Duration.ofSeconds(90);
    require(user.length() <= 16, "username exceeds 16");
    B173DedicatedServer server =
        new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout), reader = null;
    BlockPosition top, pad, westGap, westPad, powered, detector, torch;
    int column;
    BlockState idlePowered, livePowered, idleDetector, liveDetector, placedTorch;
    RemoteObjectSpawn cart;
    try {
      server.boot();
      B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3, 4},
          new int[] {1, 27, 76, 28, 328}, new int[] {32, 1, 1, 1, 1}, new int[] {0, 0, 0, 0, 0});
      actor.connect();
      actor.synchronizePose();
      require(actor.awaitInventory().occupiedSlots() == 5, "rail power inventory drift");
      RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      top = foundation(initial, cx, cz);
      column = 0;
      actor.selectHeldSlot(0);
      while (
          water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        require(++column <= 15, "water column exceeded rail power fixture");
      }
      for (int lift = 0; lift < 8; lift++) {
        top = place(actor, top, BlockFace.UP, 1);
        actor.moveAndObserve(0D, 1D, 0D, 1);
        column++;
      }
      pad = place(actor, top, BlockFace.EAST, 1);
      westGap = place(actor, top, BlockFace.WEST, 1);
      westPad = place(actor, westGap, BlockFace.WEST, 1);
      actor.selectHeldSlot(1);
      powered = BlockFace.UP.adjacent(top);
      actor.placeHeldBlock(top, BlockFace.UP);
      idlePowered = new BlockState(27, 0);
      actor.awaitBlock(powered, idlePowered);
      require(
          actor.sustainTicks(5).blockAt(powered.x(), powered.y(), powered.z()).equals(idlePowered)
              && (idlePowered.metadata() & 8) == 0,
          "live unpowered powered-rail drift");
      actor.selectHeldSlot(2);
      torch = BlockFace.UP.adjacent(pad);
      actor.placeHeldBlock(pad, BlockFace.UP);
      placedTorch = new BlockState(76, 5);
      actor.awaitBlock(torch, placedTorch);
      livePowered = new BlockState(27, 8);
      actor.awaitBlock(powered, livePowered);
      require((livePowered.metadata() & 8) != 0 && !livePowered.equals(idlePowered),
          "live powered-rail bit8 drift");
      actor.selectHeldSlot(3);
      detector = BlockFace.UP.adjacent(westPad);
      actor.placeHeldBlock(westPad, BlockFace.UP);
      idleDetector = new BlockState(28, 0);
      actor.awaitBlock(detector, idleDetector);
      require(Math.abs(powered.x() - detector.x()) >= 2 && idleDetector.legacyId() != 66
              && (idleDetector.metadata() & 8) == 0,
          "live unpowered detector rail drift");
      actor.selectHeldSlot(4);
      actor.useHeldItemOnBlock(detector, BlockFace.UP);
      cart = actor.awaitObjectSpawn(10);
      liveDetector = new BlockState(28, 8);
      require(cart.type() == 10 && cart.throwerId() == 0 && cart.velocityX() == 0
              && cart.velocityY() == 0 && cart.velocityZ() == 0
              && cart.fixedX() == detector.x() * 32 + 16 && cart.fixedY() == detector.y() * 32 + 27
              && cart.fixedZ() == detector.z() * 32 + 16,
          "minecart packet bounds drift");
      require(actor.awaitBlock(detector, liveDetector)
                  .blockAt(detector.x(), detector.y(), detector.z())
                  .equals(liveDetector)
              && (liveDetector.metadata() & 8) != 0
              && actor.sustainTicks(5)
                  .blockAt(powered.x(), powered.y(), powered.z())
                  .equals(livePowered)
              && actor.sustainTicks(5)
                  .blockAt(detector.x(), detector.y(), detector.z())
                  .equals(liveDetector),
          "live rail power drift");
      actor.close();
      awaitPlayers(server, 0);
      server.save();
      reader = new B173WireClient("127.0.0.1", port, user, timeout);
      reader.connect();
      reader.synchronizePose();
      reader.awaitBlock(powered, livePowered);
      reader.awaitBlock(detector, liveDetector);
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(powered.x(), cx), powered.y(), local(powered.z(), cz))
                  .equals(livePowered)
              && (after.blockAt(local(powered.x(), cx), powered.y(), local(powered.z(), cz))
                         .metadata()
                     & 8)
                  != 0
              && after.blockAt(local(detector.x(), cx), detector.y(), local(detector.z(), cz))
                  .equals(liveDetector)
              && (after.blockAt(local(detector.x(), cx), detector.y(), local(detector.z(), cz))
                         .metadata()
                     & 8)
                  != 0
              && after.blockAt(local(torch.x(), cx), torch.y(), local(torch.z(), cz))
                  .equals(placedTorch),
          "persisted rail power drift");
      String evidence = "column=" + column + ",support=" + top.x() + ":" + top.y() + ":" + top.z()
          + ":1:0,detectorSupport=" + westPad.x() + ":" + westPad.y() + ":" + westPad.z()
          + ":1:0,pad=" + pad.x() + ":" + pad.y() + ":" + pad.z() + ":1:0,detector=" + detector.x()
          + ":" + detector.y() + ":" + detector.z() + ":28:" + liveDetector.metadata() + ",rail="
          + powered.x() + ":" + powered.y() + ":" + powered.z() + ":27:" + livePowered.metadata()
          + ",cart=type10+thrower0+fixed" + cart.fixedX() + ":" + cart.fixedY() + ":"
          + cart.fixedZ() + ",powered=1,torch=" + torch.x() + ":" + torch.y() + ":" + torch.z()
          + ":76:" + placedTorch.metadata() + ",persisted=true,clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + seed
          + "|fixture=raised-stone+detector28+minecart328+powered-rail27+torch76|cause=packet15-item28+packet15-minecart328+packet15-item27+packet15-item76|wire=packet23-type10+thrower0+packet53-detector28:8+packet53-rail27:8+packet53-torch76:5|oracle=occupied-detector+powered-bit8+fresh-login|"
          + evidence;
      System.out.println("WORLDLINE_M309_RAIL=" + evidence);
      System.out.println("WORLDLINE_M309_TRACE=" + trace);
      System.out.println("WORLDLINE_M309_SIGNATURE=" + sha(trace));
    } finally {
      actor.close();
      if (reader != null)
        reader.close();
      server.close();
    }
  }
  private static BlockPosition place(
      B173WireClient a, BlockPosition support, BlockFace face, int id) throws Exception {
    BlockPosition target = face.adjacent(support);
    a.placeHeldBlock(support, face);
    a.awaitBlock(target, new BlockState(id, 0));
    return target;
  }
  private static BlockPosition foundation(RemoteChunkSnapshot q, int cx, int cz) {
    for (int x = 4; x <= 11; x++)
      for (int z = 4; z <= 11; z++)
        for (int y = 126; y >= 1; y--)
          if (q.blockAt(x, y, z).legacyId() == 3 && water(q.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
    throw new IllegalStateException("no deterministic rail power foundation");
  }
  private static boolean water(int id) {
    return id == 8 || id == 9;
  }
  private static int local(int v, int c) {
    return v - c * 16;
  }
  private static void awaitPlayers(B173DedicatedServer s, int n) throws Exception {
    long e = System.currentTimeMillis() + 5000;
    while (System.currentTimeMillis() < e) {
      if (s.players().size() == n)
        return;
      Thread.sleep(100);
    }
    throw new IllegalStateException("player count drift");
  }
  private static String sha(String s) throws Exception {
    byte[] b = MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8));
    StringBuilder v = new StringBuilder();
    for (byte x : b)
      v.append(String.format("%02x", x & 255));
    return v.toString();
  }
  private static void require(boolean v, String m) {
    if (!v)
      throw new IllegalStateException(m);
  }
}
