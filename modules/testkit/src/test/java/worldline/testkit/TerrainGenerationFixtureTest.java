package worldline.testkit;

import java.util.List;
import worldline.api.RemoteChunkObservation;
import worldline.api.RemoteChunkSnapshot;
import worldline.api.RemoteWorldView;

final class TerrainGenerationFixtureTest {
    private TerrainGenerationFixtureTest() { }

    static void execute() {
        byte[] blocks = new byte[32768];
        for (int x = 0; x < 16; x++) for (int z = 0; z < 16; z++) {
            blocks[index(x, 60, z)] = 1;
        }
        blocks[index(8, 20, 8)] = 14;
        blocks[index(8, 21, 8)] = 14;
        RemoteChunkObservation region = new RemoteChunkObservation(0, 0, 0, 16, 128, 16,
                81920);
        RemoteChunkSnapshot chunk = new RemoteChunkSnapshot(region, blocks,
                new byte[16384], new byte[16384], new byte[16384]);
        RemoteWorldView world = new RemoteWorldView(List.of(chunk));
        TerrainGenerationFixture.Evidence first = TerrainGenerationFixture.observe(
                world, 0, 0, 0, 0);
        TerrainGenerationFixture.Evidence second = TerrainGenerationFixture.observe(
                world, 0, 0, 0, 0);
        require(first.equals(second) && first.replayEquals(second), "evidence is not stable");
        require(first.chunks() == 1 && first.surfaceFamilies() == 1
                && first.caveAir() > 0 && first.oreBlocks() == 2
                && first.oreComponents() == 1, "terrain census drift");
        require(first.geology().matches("[0-9a-f]{64}")
                && first.describe().contains("oreVeins=1"), "canonical evidence drift");
        failure(() -> TerrainGenerationFixture.observe(world, 0, 1, 0, 0));
    }

    private static int index(int x, int y, int z) { return (x * 16 + z) * 128 + y; }
    private static void failure(Runnable action) {
        try { action.run(); throw new AssertionError("expected invalid terrain region"); }
        catch (IllegalArgumentException expected) { }
    }
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
