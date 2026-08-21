package worldline.m73.client;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.event.block.entity.BlockEntityRendererRegisterEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import worldline.m73.WorldlinePairBlockEntity;

import java.lang.invoke.MethodHandles;

/** Client-only registration of the real Aero renderer. */
public final class WorldlinePairClient {
    static { EntrypointManager.registerLookup(MethodHandles.lookup()); }
    @EventListener private static void renderer(BlockEntityRendererRegisterEvent event) {
        event.renderers.put(WorldlinePairBlockEntity.class, new WorldlinePairRenderer());
        System.out.println("[WorldlinePairContent] client-renderer-registered");
    }
}
