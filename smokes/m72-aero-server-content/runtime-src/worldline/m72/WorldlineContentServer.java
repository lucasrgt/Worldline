package worldline.m72;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.modificationstation.stationapi.api.event.tick.GameTickEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.server.event.network.PlayerPacketHandlerSetEvent;

import java.lang.invoke.MethodHandles;

/** Dedicated-server-only placement after registry sync and login settle. */
public final class WorldlineContentServer {
  private static ServerPlayerEntity pending;
  private static int ticks;
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  @EventListener
  private static void connected(PlayerPacketHandlerSetEvent event) {
    if (pending != null)
      throw new IllegalStateException("M72 supports one client");
    pending = event.player;
    ticks = 0;
    System.out.println("[WorldlineContent] player-ready name=" + pending.name);
  }
  @EventListener
  private static void tick(GameTickEvent.End event) {
    if (pending == null || ++ticks < 80)
      return;
    ServerPlayerEntity player = pending;
    pending = null;
    int x = (int) Math.floor(player.x) + 2, z = (int) Math.floor(player.z);
    int y = player.world.getTopSolidBlockY(x, z) + 1;
    if (y < 1 || y > 127)
      throw new IllegalStateException("M72 placement height invalid: " + y);
    int nonce = Integer.getInteger("worldline.content.nonce", 0);
    if (nonce <= 0)
      throw new IllegalStateException("server-only M72 nonce absent");
    if (!player.world.setBlock(x, y, z, WorldlineContentMod.block.id))
      throw new IllegalStateException("M72 server placement rejected");
    if (!(player.world.getBlockEntity(x, y, z) instanceof WorldlineContentBlockEntity entity))
      throw new IllegalStateException("M72 server BlockEntity was not created");
    entity.setNonce(nonce);
    System.out.println("[WorldlineContent] placed identifier=" + WorldlineContentMod.ID
        + ":server_probe raw=" + WorldlineContentMod.block.id + " x=" + x + " y=" + y + " z=" + z
        + " nonce=" + nonce);
  }
}
