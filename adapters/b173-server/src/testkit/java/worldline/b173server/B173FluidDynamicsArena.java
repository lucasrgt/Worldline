package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;

/** Extends the lifecycle arena with one gameplay-built gated fluid channel. */
final class B173FluidDynamicsArena {
    static final BlockPosition SOURCE_SUPPORT = B173LifecycleArena.SUPPORT;
    static final BlockPosition FLOW_SUPPORT = BlockFace.EAST.adjacent(SOURCE_SUPPORT);
    static final BlockPosition SOURCE = BlockFace.UP.adjacent(SOURCE_SUPPORT);
    static final BlockPosition FLOW = BlockFace.UP.adjacent(FLOW_SUPPORT);
    private static final BlockState STONE = new BlockState(1, 0);

    private B173FluidDynamicsArena() { }

    static B173WireClient open(B173DedicatedServer server, Path workspace, int port,
            Duration timeout, B173LifecycleLoadout loadout) throws Exception {
        B173WireClient client = B173LifecycleArena.open(
                server, workspace, port, timeout, loadout);
        try {
            client.selectHeldSlot(0);
            List<BlockPosition> floor = Arrays.asList(
                    place(client, SOURCE_SUPPORT, BlockFace.EAST),
                    place(client, SOURCE_SUPPORT, BlockFace.WEST),
                    place(client, SOURCE_SUPPORT, BlockFace.NORTH),
                    place(client, SOURCE_SUPPORT, BlockFace.SOUTH));
            BlockPosition flowNorth = place(client, FLOW_SUPPORT, BlockFace.NORTH);
            BlockPosition flowSouth = place(client, FLOW_SUPPORT, BlockFace.SOUTH);
            BlockPosition flowEast = place(client, FLOW_SUPPORT, BlockFace.EAST);
            for (BlockPosition support : Arrays.asList(floor.get(1), floor.get(2),
                    floor.get(3), flowNorth, flowSouth, flowEast)) {
                B173FixtureSupport.place(client, support, BlockFace.UP, 1);
            }
            B173FixtureSupport.place(client, FLOW_SUPPORT, BlockFace.UP, 1);
            client.awaitBlock(SOURCE_SUPPORT, STONE);
            client.awaitBlock(FLOW_SUPPORT, STONE);
            client.awaitBlock(SOURCE, new BlockState(0, 0));
            client.awaitBlock(FLOW, STONE);
            return client;
        } catch (Exception failure) {
            try { client.close(); } catch (RuntimeException close) { failure.addSuppressed(close); }
            throw failure;
        }
    }

    private static BlockPosition place(B173WireClient client, BlockPosition support,
            BlockFace face) throws Exception {
        return B173FixtureSupport.place(client, support, face, 1);
    }
}
