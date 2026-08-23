package worldline.m82;

import java.lang.invoke.MethodHandles;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.util.Identifier;
import worldline.m74.WorldlineCensusMod;

/** Common, server-safe registration for one bounded membership-wave request. */
public final class WorldlineLadderMod {
  public static final Identifier CHANGE =
      Identifier.of(WorldlineCensusMod.NAMESPACE, "membership_wave");
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  public WorldlineLadderMod() {
  }
  @EventListener
  private static void messages(MessageListenerRegistryEvent event) {
    event.register(CHANGE, (player, packet) -> {
      if (player.world.isRemote)
        WorldlineLadderState.ack(packet.ints);
      else
        WorldlineLadderServer.remove(player, packet.ints);
    });
  }
}
