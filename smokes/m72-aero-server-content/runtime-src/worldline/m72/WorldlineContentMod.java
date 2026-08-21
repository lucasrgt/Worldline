package worldline.m72;

import net.mine_diver.unsafeevents.listener.EventListener;
import net.minecraft.block.entity.BlockEntity;
import net.modificationstation.stationapi.api.event.block.entity.BlockEntityRegisterEvent;
import net.modificationstation.stationapi.api.event.registry.BlockRegistryEvent;
import net.modificationstation.stationapi.api.event.registry.MessageListenerRegistryEvent;
import net.modificationstation.stationapi.api.mod.entrypoint.EntrypointManager;
import net.modificationstation.stationapi.api.util.Identifier;
import net.modificationstation.stationapi.api.util.Namespace;

import java.lang.invoke.MethodHandles;

/** Server-safe registration and world-authored fixture for M72. */
public final class WorldlineContentMod {
    public static final String ID = "worldline-m72-content";
    public static final Namespace NAMESPACE = Namespace.resolve();
    public static final Identifier SYNC = Identifier.of(NAMESPACE, "sync_probe");
    public static WorldlineContentBlock block;
    static { EntrypointManager.registerLookup(MethodHandles.lookup()); }
    @EventListener
    private static void registerBlock(BlockRegistryEvent event) {
        block = new WorldlineContentBlock(Identifier.of(NAMESPACE, "server_probe"));
        System.out.println("[WorldlineContent] registered identifier=" + ID + ":server_probe");
    }
    @EventListener
    private static void registerBlockEntity(BlockEntityRegisterEvent event) {
        event.register.accept(WorldlineContentBlockEntity.class, ID + ":server_probe");
    }
    @EventListener
    private static void registerMessage(MessageListenerRegistryEvent event) {
        event.register(SYNC, (player, packet) -> {
            if (!player.world.isRemote) return;
            if (packet.ints == null || packet.ints.length != 4)
                throw new IllegalStateException("invalid M72 content message");
            int x = packet.ints[0], y = packet.ints[1], z = packet.ints[2], nonce = packet.ints[3];
            WorldlineContentSync.receive(player.world, x, y, z, nonce);
        });
    }
}
