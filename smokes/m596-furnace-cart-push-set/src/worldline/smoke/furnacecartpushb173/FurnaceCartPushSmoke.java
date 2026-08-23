package worldline.smoke.furnacecartpushb173;
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
import worldline.api.RemoteObjectSpawn;
import worldline.b173server.B173DedicatedServer;
import worldline.b173server.B173EntityVelocity;
import worldline.b173server.B173FurnaceCartPush;
import worldline.b173server.B173PlayerSeed;
import worldline.b173server.B173WireClient;
import worldline.test.WorldlineSmokeAwait;

/** Fuels a furnace minecart on rail 66 and proves coal consume plus south self-propulsion. */
public final class FurnaceCartPushSmoke {
  private FurnaceCartPushSmoke() {}

  public static void main(String[] args) throws Exception {
    if (args.length != 7)
      throw new IllegalArgumentException(
          "usage: FurnaceCartPushSmoke server.jar workspace port seed username chunkX chunkZ");
    Path jar = Paths.get(args[0]);
    Path workspace = Paths.get(args[1]);
    int port = Integer.parseInt(args[2]);
    long seed = Long.parseLong(args[3]);
    String user = args[4];
    int cx = Integer.parseInt(args[5]);
    int cz = Integer.parseInt(args[6]);
    require(
        seed == 17320110707L && user.equals("FurnCart596") && user.length() <= 16, "furnace-cart-push identity drift");
    Duration timeout = Duration.ofSeconds(90);
    B173DedicatedServer server = new B173DedicatedServer(jar, workspace, port, seed, timeout, 3, true);
    B173WireClient actor = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      run(server, actor, workspace, user, port, cx, cz, timeout);
    } finally {
      actor.close();
      server.close();
    }
  }

  private static void run(B173DedicatedServer server, B173WireClient actor, Path workspace, String user, int port,
      int cx, int cz, Duration timeout) throws Exception {
    server.boot();
    B173PlayerSeed.writeInventory(workspace, user, 4.5D, 60D, 4.5D, new int[] {0, 1, 2, 3}, new int[] {1, 66, 343, 263},
        new int[] {32, 8, 1, 1}, new int[] {0, 0, 0, 0});
    actor.connect();
    actor.synchronizePose();
    require(actor.awaitInventory().occupiedSlots() == 4, "furnace-cart-push inventory drift");
    RemoteChunkSnapshot initial = actor.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
    BlockPosition top = foundation(initial, cx, cz);
    int column = 0;
    actor.selectHeldSlot(0);
    while (water(initial.blockAt(local(top.x(), cx), top.y() + 1, local(top.z(), cz)).legacyId())) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      require(++column <= 15, "water column exceeded furnace-cart-push fixture");
    }
    int lift = 0;
    while (lift < 8) {
      top = place(actor, top, BlockFace.UP, 1);
      actor.moveAndObserve(0D, 1D, 0D, 1);
      column++;
      lift++;
    }
    BlockPosition northPad = place(actor, top, BlockFace.NORTH, 1);
    BlockPosition wall = place(actor, northPad, BlockFace.UP, 1);
    BlockPosition southPad = place(actor, top, BlockFace.SOUTH, 1);
    BlockPosition endPad = place(actor, southPad, BlockFace.SOUTH, 1);
    BlockPosition bumperPad = place(actor, endPad, BlockFace.SOUTH, 1);
    BlockPosition bumper = place(actor, bumperPad, BlockFace.UP, 1);
    BlockPosition westPad = place(actor, top, BlockFace.WEST, 1);
    place(actor, westPad, BlockFace.NORTH, 1);
    actor.selectHeldSlot(1);
    BlockPosition rail = BlockFace.UP.adjacent(top);
    actor.placeHeldBlock(top, BlockFace.UP);
    actor.awaitBlock(rail, new BlockState(66, 0));
    BlockPosition mid = BlockFace.UP.adjacent(southPad);
    actor.placeHeldBlock(southPad, BlockFace.UP);
    actor.awaitBlock(mid, new BlockState(66, 0));
    BlockPosition end = BlockFace.UP.adjacent(endPad);
    actor.placeHeldBlock(endPad, BlockFace.UP);
    actor.awaitBlock(end, new BlockState(66, 0));
    require(rail.x() == mid.x() && mid.x() == end.x() && mid.z() == rail.z() + 1 && end.z() == mid.z() + 1
            && wall.z() == rail.z() - 1 && bumper.z() == end.z() + 1,
        "north-south rail66 track drift");
    actor.moveAndObserve(-1D, 0D, 0D, 1);
    actor.selectHeldSlot(2);
    actor.useHeldItemOnBlock(rail, BlockFace.UP);
    RemoteObjectSpawn cart = B173FurnaceCartPush.awaitMinecart(actor);
    require(cart.type() >= 10 && cart.type() <= 12 && cart.type() != 1 && cart.throwerId() == 0 && cart.velocityX() == 0
            && cart.velocityY() == 0 && cart.velocityZ() == 0 && cart.fixedX() == rail.x() * 32 + 16
            && cart.fixedY() == rail.y() * 32 + 27 && cart.fixedZ() == rail.z() * 32 + 16
            && cart.fixedZ() != end.z() * 32 + 16,
        "furnace-cart Packet23 bounds drift");
    WorldlineSmokeAwait.observe(actor, 10);
    B173EntityVelocity idle = B173FurnaceCartPush.takeVelocity(actor, cart.entityId());
    require(idle == null || Math.abs(idle.fixedZ()) <= Math.abs(idle.fixedX()),
        "unfueled furnace cart launched on rail 66");
    actor.moveAndObserve(0D, 0D, -1D, 1);
    actor.look(180F, 0F);
    actor.selectHeldSlot(3);
    B173FurnaceCartPush.useCoal(actor, cart.entityId());
    WorldlineSmokeAwait.awaitEntity(actor,
        ()
            -> Boolean.valueOf(B173FurnaceCartPush.coalPresent(actor)),
        present -> !present.booleanValue(), "coal 263 consumed", 80);
    B173EntityVelocity push = WorldlineSmokeAwait.awaitCheckedEntity(actor,
        ()
            -> B173FurnaceCartPush.takeVelocity(actor, cart.entityId()),
        value
        -> value != null && value.fixedZ() > 0 && Math.abs(value.fixedZ()) >= Math.abs(value.fixedX()),
        "south Packet28", 80);
    require(push.entityId() == cart.entityId() && push.fixedZ() > 0, "fueled south push drift");
    actor.close();
    awaitPlayers(server, 0);
    server.save();
    B173WireClient reader = new B173WireClient("127.0.0.1", port, user, timeout);
    try {
      reader.connect();
      reader.synchronizePose();
      reader.awaitBlock(rail, new BlockState(66, 0));
      reader.awaitBlock(mid, new BlockState(66, 0));
      reader.awaitBlock(end, new BlockState(66, 0));
      RemoteChunkSnapshot after = reader.awaitRemoteChunk(cx, cz).chunkAt(cx, cz);
      require(after.blockAt(local(rail.x(), cx), rail.y(), local(rail.z(), cz)).equals(new BlockState(66, 0))
              && after.blockAt(local(mid.x(), cx), mid.y(), local(mid.z(), cz)).equals(new BlockState(66, 0))
              && after.blockAt(local(end.x(), cx), end.y(), local(end.z(), cz)).equals(new BlockState(66, 0))
              && after.blockAt(local(wall.x(), cx), wall.y(), local(wall.z(), cz)).equals(new BlockState(1, 0))
              && after.blockAt(local(bumper.x(), cx), bumper.y(), local(bumper.z(), cz)).equals(new BlockState(1, 0)),
          "persisted furnace-cart-push drift");
      String evidence = "column=" + column + ",support=" + cell(top, 1) + ",wall=" + cell(wall, 1)
          + ",bumper=" + cell(bumper, 1) + ",rail=" + cell(rail, 66) + ",track=" + cell(mid, 66)
          + ",end=" + cell(end, 66) + ",cart=type" + cart.type() + "+thrower0+fixed" + cart.fixedX() + ":"
          + cart.fixedY() + ":" + cart.fixedZ() + ",coal=263:1->0,unfueled-hold=idle,push=south"
          + ",clients=2,disconnect=clean";
      String trace = "v1|server=official-b1.7.3|seed=" + 17320110707L
          + "|fixture=raised-stone+wall+rail66+bumper+furnace-minecart343+coal263"
          + "|cause=packet15-item66+packet15-furnace-minecart343+packet7-coal263"
          + "|wire=packet23-observed-minecart+thrower0+packet103-coal263-consume+packet28-south"
          + "|oracle=unfueled-hold-idle+fueled-south-push-on-rail66-not-detector|" + evidence;
      System.out.println("WORLDLINE_M596_PUSH=" + evidence);
      System.out.println("WORLDLINE_M596_TRACE=" + trace);
      System.out.println("WORLDLINE_M596_SIGNATURE=" + sha(trace));
    } finally {
      reader.close();
    }
  }

  private static String cell(BlockPosition position, int id) {
    return position.x() + ":" + position.y() + ":" + position.z() + ":" + id + ":0";
  }

  private static BlockPosition foundation(RemoteChunkSnapshot chunk, int cx, int cz) {
    int x = 4;
    while (x <= 11) {
      int z = 4;
      while (z <= 11) {
        int y = 126;
        while (y >= 1) {
          if (chunk.blockAt(x, y, z).legacyId() == 3 && water(chunk.blockAt(x, y + 1, z).legacyId()))
            return new BlockPosition(cx * 16 + x, y, cz * 16 + z);
          y--;
        }
        z++;
      }
      x++;
    }
    throw new IllegalStateException("no deterministic furnace-cart-push foundation");
  }

  private static void require(boolean value, String message) {
    if (!value)
      throw new IllegalStateException(message);
  }
}
