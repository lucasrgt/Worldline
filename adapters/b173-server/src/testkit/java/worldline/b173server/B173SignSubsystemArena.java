package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteInventorySlot;
import worldline.api.RemoteInventoryView;

/** Builds the raised sign supports and two measured pass-through lanes through gameplay. */
final class B173SignSubsystemArena {
    static final long SEED = 17_320_110_707L;
    static final String USERNAME = "WlSignSystem";
    static final BlockPosition STANDING_SUPPORT = new BlockPosition(4, 71, 4);
    static final BlockPosition STANDING = new BlockPosition(4, 72, 4);
    static final BlockPosition WALL_SUPPORT = new BlockPosition(4, 72, 5);
    static final BlockPosition WALL = new BlockPosition(5, 72, 5);
    static final PlayerPose STANDING_ORIGIN = new PlayerPose(4D, 72D, 3.5D, 0F, 0F);
    static final PlayerPose WALL_ORIGIN = new PlayerPose(5.5D, 72D, 4D, 0F, 0F);

    private B173SignSubsystemArena() { }

    static Start open(Path workspace, int port, Duration timeout) throws Exception {
        B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                new int[] {0, 1, 2}, new int[] {1, 323, 278},
                new int[] {64, 20, 1}, new int[] {0, 0, 0});
        B173WireClient client = new B173WireClient(
                "127.0.0.1", port, USERNAME, timeout);
        try {
            client.connect(); PlayerPose pose = client.synchronizePose();
            RemoteInventoryView inventory = client.awaitInventory();
            require(inventory.occupiedSlots() == 3 && count(inventory, 323) == 20,
                    "sign inventory drift");
            RemoteChunkSnapshot initial = client.awaitRemoteChunk(0, 0).chunkAt(0, 0);
            BlockPosition top = foundation(initial); int column = 0; client.selectHeldSlot(0);
            while (B173FixtureSupport.water(
                    initial.blockAt(top.x(), top.y() + 1, top.z()).legacyId())) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                pose = client.moveAndObserve(0D, 1D, 0D, 1).resulting();
                require(++column <= 15, "sign water column exceeded fixture");
            }
            for (int lift = 0; lift < 8; lift++) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                pose = client.moveAndObserve(0D, 1D, 0D, 1).resulting(); column++;
            }
            BlockPosition north = stone(client, top, BlockFace.NORTH);
            BlockPosition east = stone(client, top, BlockFace.EAST);
            BlockPosition south = stone(client, top, BlockFace.SOUTH);
            stone(client, south, BlockFace.EAST);
            BlockPosition southTwo = stone(client, south, BlockFace.SOUTH);
            stone(client, southTwo, BlockFace.EAST);
            BlockPosition wallSupport = stone(client, south, BlockFace.UP);
            require(column == 17 && top.equals(STANDING_SUPPORT)
                    && north.equals(new BlockPosition(4, 71, 3))
                    && east.equals(new BlockPosition(5, 71, 4))
                    && wallSupport.equals(WALL_SUPPORT), "sign arena geometry drift");
            pose = move(client, pose, STANDING_ORIGIN);
            require(close(pose, STANDING_ORIGIN), "standing sign lane drift");
            require(client.sustainTicks(1).blockAt(STANDING.x(), STANDING.y(), STANDING.z())
                    .equals(new BlockState(0, 0)), "standing sign target is not air");
            return new Start(client, pose, count(client.inventory(), 323));
        } catch (Exception failure) {
            try { client.close(); } catch (RuntimeException close) { failure.addSuppressed(close); }
            throw failure;
        }
    }

    static PlayerPose move(B173WireClient client, PlayerPose from, PlayerPose target) {
        return client.moveAndObserve(target.x() - from.x(), target.y() - from.y(),
                target.z() - from.z(), 8).resulting();
    }

    static int count(RemoteInventoryView inventory, int id) {
        int count = 0;
        for (RemoteInventorySlot slot : inventory.slots()) {
            if (!slot.empty() && slot.item().legacyId() == id) count += slot.item().count();
        }
        return count;
    }

    private static BlockPosition stone(B173WireClient client,
            BlockPosition support, BlockFace face) throws Exception {
        return B173FixtureSupport.place(client, support, face, 1);
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) {
            for (int y = 126; y >= 1; y--) if (chunk.blockAt(x, y, z).legacyId() == 3
                    && B173FixtureSupport.water(chunk.blockAt(x, y + 1, z).legacyId())) {
                return new BlockPosition(x, y, z);
            }
        }
        throw new IllegalStateException("deterministic sign foundation is absent");
    }

    private static boolean close(PlayerPose left, PlayerPose right) {
        return Math.abs(left.x() - right.x()) <= 0.002D
                && Math.abs(left.y() - right.y()) <= 0.002D
                && Math.abs(left.z() - right.z()) <= 0.002D;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }

    static final class Start {
        final B173WireClient client; final PlayerPose pose; final int signs;
        Start(B173WireClient client, PlayerPose pose, int signs) {
            this.client = client; this.pose = pose; this.signs = signs;
        }
    }
}
