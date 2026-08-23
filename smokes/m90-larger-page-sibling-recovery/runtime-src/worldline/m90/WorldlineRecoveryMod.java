package worldline.m90;

import java.lang.invoke.MethodHandles;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.util.Identifier;
import worldline.m74.WorldlineCensusMod;

/** Common server-safe registration for the exact M90 remove/restore protocol. */
public final class WorldlineRecoveryMod {
  public static final Identifier CHANGE =
      Identifier.of(WorldlineCensusMod.NAMESPACE, "recovery_change");
  public static final Identifier RESTORE =
      Identifier.of(WorldlineCensusMod.NAMESPACE, "recovery_restore");
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  public WorldlineRecoveryMod() {
  }
  @EventListener
  private static void messages(MessageListenerRegistryEvent event) {
    event.register(CHANGE, (player, packet) -> {
      if (player.world.isRemote)
        WorldlineRecoveryState.ack(packet.ints);
      else
        WorldlineRecoveryServer.change(player, packet.ints);
    });
    event.register(RESTORE, (player, packet) -> {
      if (!player.world.isRemote)
        throw new IllegalStateException("M90 restore reached server");
      WorldlineRecoveryState.restore(packet.ints);
    });
  }
}
