package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteItemStack;
import worldline.api.RemoteWorldView;

/** Builds one raised three-cell light path through official gameplay actions. */
final class B173LightArena {
    static final long SEED = 17_320_110_707L;
    static final String USERNAME = "WlLight";
    static final BlockPosition SOURCE_SUPPORT = new BlockPosition(4, 71, 4);
    static final BlockPosition NEAR_SUPPORT = new BlockPosition(5, 71, 4);
    static final BlockPosition FAR_SUPPORT = new BlockPosition(6, 71, 4);
    static final BlockPosition SOURCE = new BlockPosition(4, 72, 4);
    static final BlockPosition NEAR = new BlockPosition(5, 72, 4);
    static final BlockPosition FAR = new BlockPosition(6, 72, 4);

    private B173LightArena() { }

    static Start open(Path workspace, int port, Duration timeout,
            B173LightLoadout loadout) throws Exception {
        seed(workspace, loadout);
        B173WireClient client = new B173WireClient("127.0.0.1", port, USERNAME, timeout);
        try {
            client.connect(); PlayerPose pose = client.synchronizePose();
            int occupied = loadout.support.hotbar == 0 ? 3 : 4;
            require(client.awaitInventory().occupiedSlots() == occupied, "light inventory drift");
            RemoteChunkSnapshot initial = client.awaitRemoteChunk(0, 0).chunkAt(0, 0);
            BlockPosition top = foundation(initial); int column = 0; client.selectHeldSlot(0);
            while (B173FixtureSupport.water(
                    initial.blockAt(top.x(), top.y() + 1, top.z()).legacyId())) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                pose = client.moveAndObserve(0D, 1D, 0D, 1).resulting();
                require(++column <= 15, "light water column exceeded fixture");
            }
            Raised raised = raiseSource(client, loadout, top, pose, column);
            top = raised.top; pose = raised.pose; column = raised.column;
            // A nearby gameplay-placed log keeps the leaf row out of native decay.
            client.selectHeldSlot(2);
            BlockPosition north = B173FixtureSupport.place(client, top, BlockFace.NORTH, 17);
            client.selectHeldSlot(0);
            BlockPosition near = B173FixtureSupport.place(client, top, BlockFace.EAST, 1);
            BlockPosition far = B173FixtureSupport.place(client, near, BlockFace.EAST, 1);
            require(column == 17 && top.equals(SOURCE_SUPPORT) && near.equals(NEAR_SUPPORT)
                    && far.equals(FAR_SUPPORT), "light arena geometry drift");
            pose = client.moveAndObserve((north.x() + 0.5D) - pose.x(),
                    (north.y() + 1D) - pose.y(), (north.z() + 0.5D) - pose.z(), 4).resulting();
            RemoteWorldView world = client.sustainTicks(1);
            require(world.blockAt(SOURCE.x(), SOURCE.y(), SOURCE.z()).equals(new BlockState(0, 0))
                    && world.blockAt(NEAR.x(), NEAR.y(), NEAR.z()).equals(new BlockState(0, 0))
                    && world.blockAt(FAR.x(), FAR.y(), FAR.z()).equals(new BlockState(0, 0)),
                    "light treatment path is not air");
            require(world.blockLightAt(SOURCE.x(), SOURCE.y(), SOURCE.z()) == 0
                    && world.skyLightAt(SOURCE.x(), SOURCE.y(), SOURCE.z()) == 15,
                    "light arena control planes drifted");
            return new Start(client);
        } catch (Exception failure) {
            try { client.close(); } catch (RuntimeException close) { failure.addSuppressed(close); }
            throw failure;
        }
    }

    private static void seed(Path workspace, B173LightLoadout loadout) {
        RemoteItemStack item = loadout.item;
        if (loadout.support.hotbar == 0) {
            B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                    new int[] {0, loadout.hotbar, 2}, new int[] {1, item.legacyId(), 17},
                    new int[] {48, item.count(), 1}, new int[] {0, item.damage(), 0});
            return;
        }
        RemoteItemStack support = loadout.support.item;
        B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                new int[] {0, loadout.hotbar, 2, loadout.support.hotbar},
                new int[] {1, item.legacyId(), 17, support.legacyId()},
                new int[] {48, item.count(), 1, support.count()},
                new int[] {0, item.damage(), 0, support.damage()});
    }

    private static Raised raiseSource(B173WireClient client, B173LightLoadout loadout,
            BlockPosition top, PlayerPose pose, int column) throws Exception {
        int lifts = loadout.support.hotbar == 0 ? 8 : 7;
        for (int lift = 0; lift < lifts; lift++) {
            top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
            pose = client.moveAndObserve(0D, 1D, 0D, 1).resulting();
            column++;
        }
        if (loadout.support.hotbar != 0) {
            client.selectHeldSlot(loadout.support.hotbar);
            top = B173FixtureSupport.place(client, top, BlockFace.UP, loadout.support.state);
            pose = client.moveAndObserve(0D, 1D, 0D, 1).resulting();
            column++;
            client.selectHeldSlot(0);
        }
        return new Raised(top, pose, column);
    }

    private static final class Raised {
        final BlockPosition top;
        final PlayerPose pose;
        final int column;
        Raised(BlockPosition top, PlayerPose pose, int column) {
            this.top = top;
            this.pose = pose;
            this.column = column;
        }
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++)
            for (int y = 126; y >= 1; y--) if (chunk.blockAt(x, y, z).legacyId() == 3
                    && B173FixtureSupport.water(chunk.blockAt(x, y + 1, z).legacyId())) {
                return new BlockPosition(x, y, z);
            }
        throw new IllegalStateException("deterministic light foundation is absent");
    }
    static final class Start {
        final B173WireClient client;
        Start(B173WireClient client) { this.client = client; }
    }
    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
