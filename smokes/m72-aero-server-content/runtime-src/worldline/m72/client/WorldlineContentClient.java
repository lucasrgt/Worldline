package worldline.m72.client;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.event.block.entity.BlockEntityRendererRegisterEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import worldline.m72.WorldlineContentBlockEntity;

import java.lang.invoke.MethodHandles;

/** Client-only registration of the real Aero renderer. */
public final class WorldlineContentClient {
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  @EventListener
  private static void registerRenderer(BlockEntityRendererRegisterEvent event) {
    event.renderers.put(WorldlineContentBlockEntity.class, new WorldlineContentRenderer());
    System.out.println("[WorldlineContent] client-renderer-registered");
  }
}
