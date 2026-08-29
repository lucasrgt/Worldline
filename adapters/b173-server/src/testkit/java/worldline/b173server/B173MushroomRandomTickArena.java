package worldline.b173server;

import java.nio.file.Path;
import java.time.Duration;
import worldline.api.BlockFace;
import worldline.api.BlockPosition;
import worldline.api.PlayerPose;
import worldline.api.RemoteChunkSnapshot;

/** Provisions the dark spread arena before exposing the public driver. */
final class B173MushroomRandomTickArena {
    static final long SEED = 17_320_110_707L;
    static final String USERNAME = "WlMushSpread";
    private B173MushroomRandomTickArena() { }

    static Start open(Path workspace, int port, Duration timeout,
            B173MushroomRandomTickLoadout loadout) throws Exception {
        seed(workspace, loadout);
        B173WireClient client = new B173WireClient("127.0.0.1", port, USERNAME, timeout);
        try {
            client.connect(); client.synchronizePose();
            require(client.awaitInventory().occupiedSlots() == 9,
                    "mushroom random-tick inventory drift");
            RemoteChunkSnapshot initial = client.awaitRemoteChunk(0, 0).chunkAt(0, 0);
            BlockPosition top = raise(client, initial);
            B173MushroomRandomTickStructure.build(client, top);
            PlayerPose origin = station(client, 4.5D, 72D, 3.2D);
            B173MushroomRandomTickStructure.sealLowerDoorway(client);
            client.sustainTicks(20);
            require(origin.y() >= 71.9D && origin.y() <= 72.1D,
                    "mushroom collision origin height drift");
            return new Start(client, origin);
        } catch (Exception failure) {
            try { client.close(); } catch (RuntimeException close) { failure.addSuppressed(close); }
            throw failure;
        }
    }
    private static void seed(Path workspace, B173MushroomRandomTickLoadout loadout) {
        B173PlayerSeed.writeInventory(workspace, USERNAME, 4.5D, 60D, 4.5D,
                new int[] {0, 1, 2, 3, 4, loadout.placementHotbar, 6, 7,
                        loadout.breakHotbar},
                new int[] {1, 1, 1, 1, 1, loadout.placement.legacyId(), 20, 50,
                        loadout.tool.legacyId()},
                new int[] {64, 64, 64, 64, 64, loadout.placement.count(), 8, 8,
                        loadout.tool.count()},
                new int[] {0, 0, 0, 0, 0, loadout.placement.damage(), 0, 0,
                        loadout.tool.damage()});
    }
    private static BlockPosition raise(B173WireClient client, RemoteChunkSnapshot initial)
            throws Exception {
        BlockPosition top = foundation(initial); int column = 0; client.selectHeldSlot(0);
        while (B173FixtureSupport.water(initial.blockAt(
                top.x(), top.y() + 1, top.z()).legacyId())) {
            top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
            client.moveAndObserve(0D, 1D, 0D, 1);
            require(++column <= 15, "mushroom water column exceeded fixture");
        }
        for (int lift = 0; lift < 8; lift++) {
            top = B173FixtureSupport.place(client, top, BlockFace.UP, 1);
            client.moveAndObserve(0D, 1D, 0D, 1); column++;
        }
        require(column == 17 && top.equals(B173MushroomRandomTickStructure.CENTER),
                "mushroom raised foundation drift");
        return top;
    }
    private static BlockPosition foundation(RemoteChunkSnapshot chunk) {
        for (int x = 4; x <= 11; x++) for (int z = 4; z <= 11; z++) {
            for (int y = 126; y >= 1; y--) if (chunk.blockAt(x, y, z).legacyId() == 3
                    && B173FixtureSupport.water(chunk.blockAt(x, y + 1, z).legacyId())) {
                return new BlockPosition(x, y, z);
            }
        }
        throw new IllegalStateException("deterministic mushroom foundation is absent");
    }
    private static PlayerPose station(B173WireClient client, double x, double y, double z) {
        PlayerPose pose = client.moveAndObserve(0D, 0D, 0D, 1).resulting();
        for (int attempt = 0; attempt < 16; attempt++) {
            double dx = x - pose.x(), dy = y - pose.y(), dz = z - pose.z();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (distance <= 0.05D) return pose;
            double scale = Math.min(1D, 4D / distance);
            pose = client.moveAndObserve(dx * scale, dy * scale, dz * scale, 4).resulting();
        }
        require(Math.abs(pose.x() - x) <= 0.05D && Math.abs(pose.z() - z) <= 0.05D,
                "mushroom arena station drift");
        return pose;
    }
    static final class Start {
        final B173WireClient client; final PlayerPose origin;
        Start(B173WireClient client, PlayerPose origin) { this.client = client; this.origin = origin; }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new IllegalStateException(message);
    }
}
