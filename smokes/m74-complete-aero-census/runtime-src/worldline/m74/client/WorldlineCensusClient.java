package worldline.m74.client;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.client.event.block.entity.BlockEntityRendererRegisterEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import worldline.m74.WorldlineCensusBlockEntity;
import java.lang.invoke.MethodHandles;

/** Registers only the client-side real Aero renderer. */
public final class WorldlineCensusClient {
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  @EventListener
  private static void renderer(BlockEntityRendererRegisterEvent event) {
    event.renderers.put(WorldlineCensusBlockEntity.class, new WorldlineCensusRenderer());
    System.out.println("[WorldlineCensus] client-renderer-registered");
  }
}
