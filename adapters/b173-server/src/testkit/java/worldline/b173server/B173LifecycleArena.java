package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteChunkSnapshot;
import worldline.testkit.BlockLifecycleScenario;

/** Builds the fixed public lifecycle arena through official protocol-14 actions. */
final class B173LifecycleArena {
    static final long SEED = 17_320_110_707L;
    static final BlockPosition SUPPORT = new BlockPosition(4, 71, 4);
    static final BlockState SUPPORT_STATE = new BlockState(1, 0);
    static final String USERNAME = "WlLifecycle";

    private B173LifecycleArena() { }

    static B173WireClient open(B173DedicatedServer server, Path workspace,
            int port, Duration timeout, BlockLifecycleScenario scenario) throws Exception {
        RemoteItemStack placed = scenario.placementSlot().before();
        RemoteItemStack tool = scenario.breakSlot().before();
        B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                new int[] {0, scenario.placementSlot().hotbarSlot(),
                        scenario.breakSlot().hotbarSlot()},
                new int[] {1, placed.legacyId(), tool.legacyId()},
                new int[] {32, placed.count(), tool.count()},
                new int[] {0, placed.damage(), tool.damage()});
        B173WireClient client = new B173WireClient("127.0.0.1", port, USERNAME, timeout);
        try {
            client.connect();
            client.synchronizePose();
            require(client.awaitInventory().occupiedSlots() == 3, "lifecycle inventory drift");
            RemoteChunkSnapshot initial = client.awaitRemoteChunk(0, 0).chunkAt(0, 0);
            BlockPosition top = foundation(initial);
            int column = 0;
            client.selectHeldSlot(0);
            while (B173FixtureSupport.water(
                    initial.blockAt(top.x(), top.y() + 1, top.z()).legacyId())) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                client.moveAndObserve(0D, 1D, 0D, 1);
                require(++column <= 15, "lifecycle water column exceeded fixture");
            }
            for (int lift = 0; lift < 8; lift++) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                client.moveAndObserve(0D, 1D, 0D, 1);
                column++;
            }
            require(column == 17 && top.equals(SUPPORT), "lifecycle support coordinate drift");
            client.awaitBlock(SUPPORT, SUPPORT_STATE);
            client.awaitBlock(BlockFace.UP.adjacent(SUPPORT), new BlockState(0, 0));
            return client;
        } catch (Exception failure) {
            try { client.close(); }
            catch (RuntimeException close) { failure.addSuppressed(close); }
            throw failure;
        }
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) {
            for (int y = 126; y >= 1; y--) {
                if (chunk.blockAt(x, y, z).legacyId() == 3
                        && B173FixtureSupport.water(chunk.blockAt(x, y + 1, z).legacyId())) {
                    return new BlockPosition(x, y, z);
                }
            }
        }
        throw new IllegalStateException("deterministic lifecycle foundation is absent");
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
