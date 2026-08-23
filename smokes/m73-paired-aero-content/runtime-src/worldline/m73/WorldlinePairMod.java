package worldline.m73;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.modificationstation.stationapi.api.event.block.entity.BlockEntityRegisterEvent;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;

import java.lang.invoke.MethodHandles;

/** Identifier-scoped registration and tracked-plan M73 protocol. */
public final class WorldlinePairMod {
  public static final String ID = "worldline-m73-content";
  public static final Namespace NAMESPACE = Namespace.resolve();
  public static final Identifier ACTIVATE = Identifier.of(NAMESPACE, "activate");
  public static final Identifier PLAN = Identifier.of(NAMESPACE, "plan");
  public static final Identifier READY = Identifier.of(NAMESPACE, "ready");
  public static final Identifier SYNC = Identifier.of(NAMESPACE, "sync_cell");
  public static WorldlinePairBlock block;
  static {
    EntrypointManager.registerLookup(MethodHandles.lookup());
  }
  @EventListener
  private static void block(BlockRegistryEvent event) {
    block = new WorldlinePairBlock(Identifier.of(NAMESPACE, "paired_probe"));
    System.out.println("[WorldlinePairContent] registered identifier=" + ID + ":paired_probe");
  }
  @EventListener
  private static void blockEntity(BlockEntityRegisterEvent event) {
    event.register.accept(WorldlinePairBlockEntity.class, ID + ":paired_probe");
  }
  @EventListener
  private static void messages(MessageListenerRegistryEvent event) {
    event.register(ACTIVATE, (player, packet) -> {
      if (player.world.isRemote)
        throw new IllegalStateException("M73 activation reached client");
      WorldlinePairServer.activate(player, packet.ints);
    });
    event.register(PLAN, (player, packet) -> {
      if (!player.world.isRemote)
        throw new IllegalStateException("M73 plan reached server");
      WorldlinePairSync.plan(packet.ints);
    });
    event.register(READY, (player, packet) -> {
      if (player.world.isRemote)
        throw new IllegalStateException("M73 ready reached client");
      WorldlinePairServer.ready(player, packet.ints);
    });
    event.register(SYNC, (player, packet) -> {
      if (!player.world.isRemote)
        throw new IllegalStateException("M73 sync reached server");
      if (packet.ints == null || packet.ints.length != 4)
        throw new IllegalStateException("invalid M73 sync shape");
      WorldlinePairSync.receive(
          player.world, packet.ints[0], packet.ints[1], packet.ints[2], packet.ints[3]);
    });
  }
}
