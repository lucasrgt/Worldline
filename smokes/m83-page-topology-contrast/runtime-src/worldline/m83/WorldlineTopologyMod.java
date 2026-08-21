package worldline.m83;

import java.lang.invoke.MethodHandles;import net.mine_diver.unsafeevents.listener.EventListener;import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;import net.modificationstation.stationapi.api.util.Identifier;import worldline.m74.WorldlineCensusMod;

/** Common registration for one same-page or cross-page two-member request. */
public final class WorldlineTopologyMod {
    public static final Identifier CHANGE=Identifier.of(WorldlineCensusMod.NAMESPACE,"page_topology");static{EntrypointManager.registerLookup(MethodHandles.lookup());}public WorldlineTopologyMod(){}
    @EventListener private static void messages(MessageListenerRegistryEvent event){event.register(CHANGE,(player,packet)->{if(player.world.isRemote)WorldlineTopologyState.ack(packet.ints);else WorldlineTopologyServer.remove(player,packet.ints);});}
}
