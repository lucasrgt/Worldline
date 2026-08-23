package worldline.m80;

import java.lang.invoke.MethodHandles;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.util.Identifier;
import worldline.m74.WorldlineCensusMod;

/** Common, server-safe registration for one membership-removal request/ack. */
public final class WorldlineMembershipMod {
  public static final Identifier CHANGE =
      Identifier.of(WorldlineCensusMod.NAMESPACE, "membership_change");
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  public WorldlineMembershipMod() {
  }
  @EventListener
  private static void messages(MessageListenerRegistryEvent event) {
    event.register(CHANGE, (player, packet) -> {
      if (player.world.isRemote)
        WorldlineMembershipState.ack(packet.ints);
      else
        WorldlineMembershipServer.remove(player, packet.ints);
    });
  }
}
