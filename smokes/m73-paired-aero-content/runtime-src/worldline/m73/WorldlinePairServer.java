package worldline.m73;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.Vec3i;
import net.modificationstation.stationapi.api.network.packet.MessagePacket;
import net.modificationstation.stationapi.api.event.tick.GameTickEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.server.event.network.PlayerPacketHandlerSetEvent;

import java.lang.invoke.MethodHandles;

/** Schedules the identical activation request onto the next server tick. */
public final class WorldlinePairServer {
  private static ServerPlayerEntity player;
  private static boolean awaiting, scheduled, completed;
  private static int planX, planY, planZ, delay, phase, placed;
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  @EventListener
  private static void connected(PlayerPacketHandlerSetEvent event) {
    if (player != null)
      throw new IllegalStateException("M73 supports one client");
    player = event.player;
    System.out.println("[WorldlinePairContent] player-ready name=" + player.name);
  }
  public static synchronized void activate(PlayerEntity source, int[] values) {
    int expected = Integer.getInteger("worldline.pair.nonce", 0);
    if (source != player || values == null || values.length != 1 || values[0] != expected
        || expected <= 0)
      throw new IllegalStateException("invalid M73 activation");
    if (awaiting || scheduled || completed)
      throw new IllegalStateException("duplicate M73 activation");
    Integer fixedX = Integer.getInteger("worldline.pair.planX"),
            fixedY = Integer.getInteger("worldline.pair.planY"),
            fixedZ = Integer.getInteger("worldline.pair.planZ");
    if ((fixedX == null) != (fixedY == null) || (fixedX == null) != (fixedZ == null))
      throw new IllegalStateException("partial M73 plan");
    Vec3i spawn = player.world.getSpawnPos();
    planX = fixedX == null ? spawn.x + 2 : fixedX;
    planZ = fixedZ == null ? spawn.z - 2 : fixedZ;
    planY = fixedY == null ? 1 : fixedY;
    if (fixedY == null)
      for (int dz = 0; dz < 4; dz++)
        planY = Math.max(planY, player.world.getTopSolidBlockY(planX, planZ + dz) + 1);
    player.networkHandler.teleport(planX - 1.5D, planY, planZ + 2.5D, -90.0F, 0.0F);
    MessagePacket plan = new MessagePacket(WorldlinePairMod.PLAN);
    plan.ints = new int[] {planX, planY, planZ};
    player.networkHandler.sendPacket(plan);
    awaiting = true;
    phase = placed = 0;
    System.out.println("[WorldlinePairContent] activation nonce=" + expected);
  }
  public static synchronized void ready(PlayerEntity source, int[] values) {
    if (source != player || !awaiting || scheduled || completed || values == null
        || values.length != 3 || values[0] != planX || values[1] != planY || values[2] != planZ)
      throw new IllegalStateException("invalid M73 tracking readiness");
    awaiting = false;
    scheduled = true;
    delay = 2;
    System.out.println(
        "[WorldlinePairContent] tracking-ready x=" + planX + " baseY=" + planY + " baseZ=" + planZ);
  }
  @EventListener
  private static synchronized void tick(GameTickEvent.End event) {
    if (!scheduled || delay-- > 0)
      return;
    String mode = System.getProperty("worldline.pair.mode", "");
    if (!(mode.equals("present") || mode.equals("absent")))
      throw new IllegalStateException("invalid M73 arm");
    if (phase == 0)
      for (int dz = 0; dz < 4; dz++)
        for (int dy = 0; dy < 4; dy++)
          if (player.world.getBlockId(planX, planY + dy, planZ + dz) != 0)
            throw new IllegalStateException("M73 planned cell occupied");
    int root = Integer.getInteger("worldline.pair.nonce", 0), dz = phase;
    if (mode.equals("present"))
      for (int dy = 0; dy < 4; dy++) {
        int z = planZ + dz, y = planY + dy;
        if (!player.world.setBlock(planX, y, z, WorldlinePairMod.block.id))
          throw new IllegalStateException("M73 placement rejected");
        if (!(player.world.getBlockEntity(planX, y, z) instanceof WorldlinePairBlockEntity be))
          throw new IllegalStateException("M73 BE absent");
        be.setNonce(root * 100 + dz * 4 + dy + 1);
        player.networkHandler.sendPacket(be.createUpdatePacket());
        placed++;
      }
    if (++phase < 4)
      return;
    scheduled = false;
    completed = true;
    System.out.println("[WorldlinePairContent] scene mode=" + mode + " planned=16 placed=" + placed
        + " raw=" + WorldlinePairMod.block.id + " x=" + planX + " baseY=" + planY
        + " baseZ=" + planZ + " yaw=-90 pitch=0 nonce=" + root);
  }
}
