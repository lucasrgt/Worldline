package worldline.m74;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3i;
import net.modificationstation.stationapi.api.event.tick.GameTickEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import net.modificationstation.stationapi.api.server.event.network.PlayerPacketHandlerSetEvent;
import java.lang.invoke.MethodHandles;

/** Owns the paired plan, fixed camera, four phases, and explicit scene seal. */
public final class WorldlineCensusServer {
  private static ServerPlayerEntity player;
  private static boolean awaiting, scheduled, completed;
  private static int x, y, z, delay, phase, placed;
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  @EventListener
  private static void connected(PlayerPacketHandlerSetEvent event) {
    if (player != null)
      throw new IllegalStateException("M74 supports one client");
    player = event.player;
    System.out.println("[WorldlineCensus] player-ready name=" + player.name);
  }
  public static synchronized void activate(PlayerEntity source, int[] values) {
    int nonce = Integer.getInteger("worldline.census.nonce", 0);
    if (source != player || values == null || values.length != 1 || values[0] != nonce
        || nonce <= 0)
      throw new IllegalStateException("invalid M74 activation");
    if (awaiting || scheduled || completed)
      throw new IllegalStateException("duplicate M74 activation");
    Integer fx = Integer.getInteger("worldline.census.planX"),
            fy = Integer.getInteger("worldline.census.planY"),
            fz = Integer.getInteger("worldline.census.planZ");
    if ((fx == null) != (fy == null) || (fx == null) != (fz == null))
      throw new IllegalStateException("partial M74 plan");
    Vec3i spawn = player.world.getSpawnPos();
    x = fx == null ? spawn.x + 2 : fx;
    z = fz == null ? spawn.z - 2 : fz;
    y = fy == null ? 1 : fy;
    if (fy == null) {
      for (int dz = 0; dz < 4; dz++)
        y = Math.max(y, player.world.getTopSolidBlockY(x, z + dz) + 1);
      y = Math.max(y, player.world.getTopSolidBlockY(x - 2, z + 2) + 1);
    }
    player.world.setBlock(x - 2, y - 1, z + 2, 1);
    if (player.world.getBlockId(x - 2, y - 1, z + 2) != 1)
      throw new IllegalStateException("M74 camera support rejected");
    player.networkHandler.teleport(x - 1.5D, y, z + 2.5D, -90F, 0F);
    awaiting = true;
    phase = -1;
    placed = 0;
    delay = 1;
    System.out.println("[WorldlineCensus] activation nonce=" + nonce);
  }
  public static synchronized void ready(PlayerEntity source, int[] values) {
    if (source != player || !awaiting || values == null || values.length != 3 || values[0] != x
        || values[1] != y || values[2] != z)
      throw new IllegalStateException("invalid M74 readiness");
    awaiting = false;
    scheduled = true;
    delay = 2;
    System.out.println("[WorldlineCensus] tracking-ready x=" + x + " baseY=" + y + " baseZ=" + z);
  }
  @EventListener
  private static synchronized void tick(GameTickEvent.End event) {
    if (awaiting && phase == -1) {
      if (delay-- > 0)
        return;
      MessagePacket plan = new MessagePacket(WorldlineCensusMod.PLAN);
      plan.ints = new int[] {x, y, z};
      player.networkHandler.sendPacket(plan);
      phase = 0;
      System.out.println("[WorldlineCensus] plan-sent");
      return;
    }
    if (!scheduled || delay-- > 0)
      return;
    String mode = System.getProperty("worldline.census.mode", "");
    if (!(mode.equals("present") || mode.equals("absent")))
      throw new IllegalStateException("invalid M74 arm");
    if (phase == 0)
      for (int dz = 0; dz < 4; dz++)
        for (int dy = 0; dy < 4; dy++)
          if (player.world.getBlockId(x, y + dy, z + dz) != 0)
            throw new IllegalStateException("M74 planned cell occupied");
    int root = Integer.getInteger("worldline.census.nonce", 0), dz = phase;
    if (mode.equals("present"))
      for (int dy = 0; dy < 4; dy++) {
        int cy = y + dy, cz = z + dz;
        if (!player.world.setBlock(x, cy, cz, WorldlineCensusMod.block.id))
          throw new IllegalStateException("M74 placement rejected");
        if (!(player.world.getBlockEntity(x, cy, cz) instanceof WorldlineCensusBlockEntity be))
          throw new IllegalStateException("M74 BE absent");
        be.setNonce(root * 100 + dz * 4 + dy + 1);
        player.networkHandler.sendPacket(be.createUpdatePacket());
        placed++;
      }
    if (++phase < 4)
      return;
    scheduled = false;
    completed = true;
    MessagePacket scene = new MessagePacket(WorldlineCensusMod.SCENE);
    scene.ints = new int[] {x, y, z, root, placed};
    player.networkHandler.sendPacket(scene);
    System.out.println("[WorldlineCensus] scene mode=" + mode + " planned=16 placed=" + placed
        + " raw=" + WorldlineCensusMod.block.id + " x=" + x + " baseY=" + y + " baseZ=" + z
        + " yaw=-90 pitch=0 nonce=" + root);
  }
}
