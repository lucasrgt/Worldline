package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteChunkSnapshot;

/** Builds the fixed public lifecycle arena through official protocol-14 actions. */
final class B173LifecycleArena {
    static final long SEED = 17_320_110_707L;
    static final BlockPosition SUPPORT = new BlockPosition(4, 71, 4);
    static final BlockState SUPPORT_STATE = new BlockState(1, 0);
    static final String USERNAME = "WlLifecycle";

    private B173LifecycleArena() { }

    static B173WireClient open(B173DedicatedServer server, Path workspace,
            int port, Duration timeout, B173LifecycleLoadout loadout) throws Exception {
        RemoteItemStack placed = loadout.placement;
        RemoteItemStack tool = loadout.tool;
        seed(workspace, loadout, placed, tool);
        B173WireClient client = new B173WireClient("127.0.0.1", port, USERNAME, timeout);
        try {
            client.connect();
            client.synchronizePose();
            int occupied = loadout.supportHotbar == 0 ? 3 : 4;
            require(client.awaitInventory().occupiedSlots() == occupied,
                    "lifecycle inventory drift");
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
            for (int lift = 0; lift < 7; lift++) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                client.moveAndObserve(0D, 1D, 0D, 1);
                column++;
            }
            client.selectHeldSlot(loadout.supportHotbar);
            top = B173FixtureSupport.place(client, top, BlockFace.UP,
                    loadout.support.legacyId());
            client.moveAndObserve(0D, 1D, 0D, 1);
            column++;
            require(column == 17 && top.equals(SUPPORT), "lifecycle support coordinate drift");
            client.awaitBlock(SUPPORT, new BlockState(loadout.support.legacyId(),
                    loadout.support.damage()));
            BlockPosition target = BlockFace.UP.adjacent(SUPPORT);
            client.awaitBlock(target, new BlockState(0, 0));
            if (loadout.overhead != null) shade(client, loadout, target);
            return client;
        } catch (Exception failure) {
            try { client.close(); }
            catch (RuntimeException close) { failure.addSuppressed(close); }
            throw failure;
        }
    }

    private static void shade(B173WireClient client, B173LifecycleLoadout loadout,
            BlockPosition target) throws Exception {
        require(loadout.overhead.equals(SUPPORT_STATE),
                "official lifecycle provider only provisions stone overhead");
        client.selectHeldSlot(0);
        BlockPosition east = B173FixtureSupport.place(client, SUPPORT, BlockFace.EAST, 1);
        BlockPosition west = B173FixtureSupport.place(client, SUPPORT, BlockFace.WEST, 1);
        BlockPosition north = B173FixtureSupport.place(client, SUPPORT, BlockFace.NORTH, 1);
        BlockPosition south = B173FixtureSupport.place(client, SUPPORT, BlockFace.SOUTH, 1);
        BlockPosition wall = B173FixtureSupport.place(client, east, BlockFace.UP, 1);
        B173FixtureSupport.place(client, west, BlockFace.UP, 1);
        B173FixtureSupport.place(client, north, BlockFace.UP, 1);
        B173FixtureSupport.place(client, south, BlockFace.UP, 1);
        BlockPosition overhead = B173FixtureSupport.place(client,
                B173FixtureSupport.place(client, wall, BlockFace.UP, 1), BlockFace.WEST, 1);
        require(overhead.equals(BlockFace.UP.adjacent(target)),
                "lifecycle overhead coordinate drift");
        client.awaitBlock(target, new BlockState(0, 0));
    }

    private static void seed(Path workspace, B173LifecycleLoadout loadout,
            RemoteItemStack placed, RemoteItemStack tool) throws Exception {
        if (loadout.supportHotbar == 0) {
            B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                    new int[] {0, loadout.placementHotbar, loadout.breakHotbar},
                    new int[] {1, placed.legacyId(), tool.legacyId()},
                    new int[] {32, placed.count(), tool.count()},
                    new int[] {0, placed.damage(), tool.damage()});
            return;
        }
        B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                new int[] {0, loadout.placementHotbar, loadout.breakHotbar,
                        loadout.supportHotbar},
                new int[] {1, placed.legacyId(), tool.legacyId(),
                        loadout.support.legacyId()},
                new int[] {32, placed.count(), tool.count(), loadout.support.count()},
                new int[] {0, placed.damage(), tool.damage(), loadout.support.damage()});
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
