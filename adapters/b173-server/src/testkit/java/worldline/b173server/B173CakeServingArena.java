package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.BlockState;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;

/** Builds the raised cake support and measured collision lane through gameplay actions. */
final class B173CakeServingArena {
    static final long SEED = 17_320_110_707L;
    static final String USERNAME = "WlCakeServing";
    static final BlockPosition SUPPORT = new BlockPosition(4, 71, 4);
    static final BlockPosition TARGET = new BlockPosition(4, 72, 4);

    private B173CakeServingArena() { }

    static Start open(Path workspace, int port, Duration timeout) throws Exception {
        B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                new int[] {0, 1, 2}, new int[] {1, 354, 278},
                new int[] {48, 2, 1}, new int[] {0, 0, 0}, 1);
        B173WireClient client = new B173WireClient(
                "127.0.0.1", port, USERNAME, timeout);
        try {
            client.connect(); PlayerPose pose = client.synchronizePose();
            require(client.awaitInventory().occupiedSlots() == 3, "cake inventory drift");
            require(client.awaitHealth(1) == 1, "cake health baseline drift");
            RemoteChunkSnapshot initial = client.awaitRemoteChunk(0, 0).chunkAt(0, 0);
            BlockPosition top = foundation(initial); int column = 0; client.selectHeldSlot(0);
            while (B173FixtureSupport.water(
                    initial.blockAt(top.x(), top.y() + 1, top.z()).legacyId())) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                pose = client.moveAndObserve(0D, 1D, 0D, 1).resulting();
                require(++column <= 15, "cake water column exceeded fixture");
            }
            for (int lift = 0; lift < 8; lift++) {
                top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
                pose = client.moveAndObserve(0D, 1D, 0D, 1).resulting(); column++;
            }
            BlockPosition north = B173FixtureSupport.place(client, top, BlockFace.NORTH, 1);
            require(column == 17 && top.equals(SUPPORT)
                    && north.equals(new BlockPosition(4, 71, 3)), "cake arena geometry drift");
            pose = client.moveAndObserve(north.x() + 0.5D - pose.x(),
                    north.y() + 1D - pose.y(), north.z() + 0.5D - pose.z(), 4).resulting();
            pose = client.moveAndObserve(-0.5D, 0D, 0D, 4).resulting();
            require(close(pose.x(), 4D) && close(pose.y(), 72D) && close(pose.z(), 3.5D),
                    "cake collision lane drift");
            RemoteWorldView world = client.sustainTicks(1);
            require(world.blockAt(TARGET.x(), TARGET.y(), TARGET.z())
                    .equals(new BlockState(0, 0)), "cake target is not air");
            return new Start(client, pose);
        } catch (Exception failure) {
            try { client.close(); } catch (RuntimeException close) { failure.addSuppressed(close); }
            throw failure;
        }
    }

    private static BlockPosition foundation(RemoteChunkSnapshot chunk) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) {
            for (int y = 126; y >= 1; y--) if (chunk.blockAt(x, y, z).legacyId() == 3
                    && B173FixtureSupport.water(chunk.blockAt(x, y + 1, z).legacyId())) {
                return new BlockPosition(x, y, z);
            }
        }
        throw new IllegalStateException("deterministic cake foundation is absent");
    }

    private static boolean close(double left, double right) {
        return Math.abs(left - right) <= 0.002D;
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
    static final class Start {
        final B173WireClient client; final PlayerPose origin;
        Start(B173WireClient client, PlayerPose origin) {
            this.client = client; this.origin = origin;
        }
    }
}
