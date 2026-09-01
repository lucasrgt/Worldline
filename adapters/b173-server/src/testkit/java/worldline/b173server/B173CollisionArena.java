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

/** Builds a raised air-control and collision path through official gameplay actions. */
final class B173CollisionArena {
    static final long SEED = 17_320_110_707L;
    static final String USERNAME = "WlCollision";
    static final BlockPosition TARGET_SUPPORT = new BlockPosition(4, 71, 4);
    static final BlockPosition EAST_SUPPORT = new BlockPosition(5, 71, 4);
    static final BlockPosition ORIGIN_SUPPORT = new BlockPosition(4, 71, 3);

    private B173CollisionArena() { }

    static Start open(B173DedicatedServer server, Path workspace, int port, Duration timeout,
            B173CollisionLoadout loadout) throws Exception {
        seed(workspace, loadout);
        B173WireClient client = new B173WireClient("127.0.0.1", port, USERNAME, timeout);
        try {
            client.connect(); PlayerPose pose = client.synchronizePose();
            int occupied = loadout.support.hotbar == 0 ? 2 : 3;
            require(client.awaitInventory().occupiedSlots() == occupied,
                    "collision inventory drift");
            RemoteChunkSnapshot initial = client.awaitRemoteChunk(0, 0).chunkAt(0, 0);
            BlockPosition top = foundation(initial); int column = 0;
            client.selectHeldSlot(0);
            while (B173FixtureSupport.water(
                    initial.blockAt(top.x(), top.y() + 1, top.z()).legacyId())) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                pose = client.moveAndObserve(0D, 1D, 0D, 1).resulting();
                require(++column <= 15, "collision water column exceeded fixture");
            }
            Raised raised = raiseTarget(client, loadout, top, pose, column);
            top = raised.top; pose = raised.pose; column = raised.column;
            BlockPosition north = B173FixtureSupport.place(client, top, BlockFace.NORTH, 1);
            BlockPosition east = B173FixtureSupport.place(client, top, BlockFace.EAST, 1);
            require(column == 17 && top.equals(TARGET_SUPPORT) && north.equals(ORIGIN_SUPPORT)
                    && east.equals(EAST_SUPPORT), "collision arena geometry drift");
            pose = client.moveAndObserve((north.x() + 0.5D) - pose.x(),
                    (north.y() + 1D) - pose.y(), (north.z() + 0.5D) - pose.z(), 4).resulting();
            RemoteWorldView world = client.sustainTicks(1);
            require(world.blockAt(4, 72, 4).equals(new BlockState(0, 0))
                    && world.blockAt(5, 72, 4).equals(new BlockState(0, 0)),
                    "collision treatment path is not air");
            return new Start(client, pose);
        } catch (Exception failure) {
            try { client.close(); } catch (RuntimeException close) { failure.addSuppressed(close); }
            throw failure;
        }
    }

    private static void seed(Path workspace, B173CollisionLoadout loadout) {
        RemoteItemStack item = loadout.item;
        if (loadout.support.hotbar == 0) {
            B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                    new int[] {0, loadout.hotbar}, new int[] {1, item.legacyId()},
                    new int[] {48, item.count()}, new int[] {0, item.damage()});
            return;
        }
        RemoteItemStack support = loadout.support.item;
        B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                new int[] {0, loadout.hotbar, loadout.support.hotbar},
                new int[] {1, item.legacyId(), support.legacyId()},
                new int[] {48, item.count(), support.count()},
                new int[] {0, item.damage(), support.damage()});
    }

    private static Raised raiseTarget(B173WireClient client, B173CollisionLoadout loadout,
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
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) {
            for (int y = 126; y >= 1; y--) if (chunk.blockAt(x, y, z).legacyId() == 3
                    && B173FixtureSupport.water(chunk.blockAt(x, y + 1, z).legacyId())) {
                return new BlockPosition(x, y, z);
            }
        }
        throw new IllegalStateException("deterministic collision foundation is absent");
    }

    static final class Start {
        final B173WireClient client; final PlayerPose origin;
        Start(B173WireClient client, PlayerPose origin) {
            this.client = client; this.origin = origin;
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
