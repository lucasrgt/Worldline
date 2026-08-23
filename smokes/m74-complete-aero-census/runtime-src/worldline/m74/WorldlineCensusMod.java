package worldline.m74;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.block.entity.BlockEntityRegisterEvent;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;
import java.lang.invoke.MethodHandles;

/** Identifier-scoped registration and four-channel M74 protocol. */
public final class WorldlineCensusMod {
  public static final String ID = "worldline-m74-content";
  public static final Namespace NAMESPACE = Namespace.resolve();
  public static final Identifier ACTIVATE = Identifier.of(NAMESPACE, "activate"),
                                 PLAN = Identifier.of(NAMESPACE, "plan"),
                                 READY = Identifier.of(NAMESPACE, "ready"),
                                 SYNC = Identifier.of(NAMESPACE, "sync_cell"), SCENE = PLAN;
  public static WorldlineCensusBlock block;
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  @EventListener
  private static void block(BlockRegistryEvent event) {
    block = new WorldlineCensusBlock(Identifier.of(NAMESPACE, "census_probe"));
    System.out.println("[WorldlineCensus] registered identifier=" + ID + ":census_probe");
  }
  @EventListener
  private static void blockEntity(BlockEntityRegisterEvent event) {
    event.register.accept(WorldlineCensusBlockEntity.class, ID + ":census_probe");
  }
  @EventListener
  private static void messages(MessageListenerRegistryEvent event) {
    event.register(ACTIVATE, (player, packet) -> {
      if (player.world.isRemote)
        throw new IllegalStateException("M74 activation reached client");
      WorldlineCensusServer.activate(player, packet.ints);
    });
    event.register(PLAN, (player, packet) -> {
      if (!player.world.isRemote)
        throw new IllegalStateException("M74 plan reached server");
      if (packet.ints != null && packet.ints.length == 3)
        WorldlineCensusSync.plan(packet.ints);
      else
        WorldlineCensusSync.scene(packet.ints);
    });
    event.register(READY, (player, packet) -> {
      if (player.world.isRemote)
        throw new IllegalStateException("M74 ready reached client");
      WorldlineCensusServer.ready(player, packet.ints);
    });
    event.register(SYNC, (player, packet) -> {
      if (!player.world.isRemote || packet.ints == null || packet.ints.length != 4)
        throw new IllegalStateException("invalid M74 sync");
      WorldlineCensusSync.receive(
          player.world, packet.ints[0], packet.ints[1], packet.ints[2], packet.ints[3]);
    });
  }
}
