package worldline.m81;

import java.lang.invoke.MethodHandles;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.util.Identifier;
import worldline.m74.WorldlineCensusMod;

/** Common, server-safe registration for one multipage-removal request/ack. */
public final class WorldlineWaveMod {
  public static final Identifier CHANGE =
      Identifier.of(WorldlineCensusMod.NAMESPACE, "multipage_change");
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  public WorldlineWaveMod() {
  }
  @EventListener
  private static void messages(MessageListenerRegistryEvent event) {
    event.register(CHANGE, (player, packet) -> {
      if (player.world.isRemote)
        WorldlineWaveState.ack(packet.ints);
      else
        WorldlineWaveServer.remove(player, packet.ints);
    });
  }
}
